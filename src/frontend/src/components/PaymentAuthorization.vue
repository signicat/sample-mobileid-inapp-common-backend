<template>
  <div> <!-- Div auto removed when rendered - cannot use for page ID -->
    <div id="paymentRoutePage"/>
    <p class="header-description">MobileID Sample</p>
    <h1>Web to merchant app integration</h1>
    <h2>Payment authorization</h2>
    <h3>
      <span>1 - Enter the externalRef of the user you want to authenticate</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about externalRef" @click="$showTip($event, 'show_hide_basic_info')"></a>
    </h3>
    <div id="show_hide_basic_info" class="info-text-box">
      <p>
      <ul><li>The 'last used' <code>externalRef</code> is suggested in the text box below</li></ul>
      </p>
    </div>

    <p>
      <label>External reference</label>
    </p>
    <input v-model="externalRef" id="externalRef" type='medium-text-box' value="" aria-label="External reference"/>


    <br>

    <h3>
      <span>2 - Click the <b>Get available devices</b> button and select an authentication device</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about available devices" @click="$showTip($event, 'show_hide_get_devices')"></a>
    </h3>
    <div id="show_hide_get_devices" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the request to the sample backend (<code>getDevices</code>) along with the <code>externalRef</code></li>
          <li>The sample backend fetches all devices (of type <code>MOBILEID</code>) that are activated for the chosen <code>externalRef</code></li>
          <ul><li>SOAP request <code>getDevices</code> is made to Signicat</li></ul>
        <li>The list of device names is displayed in the list below</li>
      </ul>
      </p>
    </div>
    <div>
      <button class="button" @click="getDevices">Get available devices</button>
    </div>

    <p>
      <label>Available devices</label>
    </p>
    <select v-model="selectedDevice" class="signicat-select" text="Choose a device to Authenticate" aria-label="Available devices">
      <option v-for="device in deviceList" v-bind:key="device.index"  v-bind:value="device.value">
        {{ device.value }}
      </option>
    </select>

    <br>
    <br>
    <h3>
      <span>3 - Optionally enter additional information to be passed back to the app</span>
    </h3>
    <p>
      <input type="checkbox" id="pushPayloadCheck" v-model="pushPayloadChecked"/>
      <label for="pushPayloadCheck"> Specify push payload</label>
      <input v-model="pushPayload" type='medium-text-box' value="" placeholder="Push message payload" v-if="pushPayloadChecked"/>
    </p>

    <br>
    <br>
    <h3>
      <span>4 - Enter payment information and click the <b>Authorize</b> button</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about payment authorization" @click="$showTip($event, 'show_hide_select_device')"></a>
    </h3>
    <div id="show_hide_select_device" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the payment authorization request to the sample backend (<code>startPaymentAuthorization</code>)</li>
        <li>Sample backend</li>
          <ul>
            <li>Obtains <code>deviceId</code> for the selected device name</li>
            <li>Collects all necessary data and generates <code>authorizationUrl</code></li>
            <li>Initiates authorization code grant flow towards Signicat</li>
            <li>Sends response back to the client (with <code>statusUrl</code> and <code>completeUrl</code> links)</li>
          </ul>
      </ul>
      </p>
    </div>

    <p>
      <label>Payment information</label>
      <input v-model="authContextMsg" type='medium-text-box' value="Enter payment information" aria-label="Payment information"/>
    </p>

    <div>
      <button class="button" @click="startPaymentAuthorization">Authorize</button>
    </div>

    <br>
    <br>
    <h3>
      <span>5 - Push notification is displayed on the mobile device. Carry out authorization</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about push notification" @click="$showTip($event, 'show_hide_push_info')"></a>
    </h3>
    <div id="show_hide_push_info" class="info-text-box">
      <p>
      <ul >
        <li>The client executes polling calls to the sample backend's <code>/authorizepayment/checkStatus</code> endpoint</li>
      <ul><li>The sample backend uses the received <code>statusUrl</code> and executes a call to Signicat</li></ul>
      <li>When authorization is fulfilled, the client calls the sample backend's <code>/authorizepayment/doComplete</code> endpoint</li>
          <ul><li>The sample backend executes a call to the <code>completeUrl</code></li></ul>
        <li>Signicat answers with the <code>authorizationCode</code> to the <code>redirect_uri</code>.
        <li>The sample backend carries out regular token exchange and fetches <code>userinfo</code></li>
        <li>The result is shown in the field below</li>
      </ul>
      </p>
    </div>

    <p>
      <label>Payment authorization response</label>
    </p>
    <textarea v-model="response" id="authorizepaymentCompleteResponse" disabled="disabled" aria-label="Payment authorization response"></textarea>

  </div>
</template>

<script>
export default {
  name: 'PaymentAuthorization',
  data() {
    return {
      deviceId : "",
      externalRef : "",
      response : "",
      authContextMsg : "",
      selectedDevice: '',
      deviceList: [],
      servicePath : this.$store.state.servicePath,
      pushPayload: '',
      pushPayloadChecked: false
    }
  },
  beforeMount() {
    this.init()
  },
  methods : {
    init : async function() {
      const initResponse = await fetch('/mobileid-inapp' + this.servicePath +'/authorizepayment/init')  ;
      if (initResponse.ok) {
        this.externalRef = await initResponse.text();
      } else {
        throw Error(initResponse.statusText);
      }
    },
    startPaymentAuthorization : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      let u = this.selectedDevice
      let authUrl = '/mobileid-inapp' + servicePath + '/authorizepayment/start?externalRef='+ extRef
          + '&deviceName=' + u;

      if (this.pushPayloadChecked === true) {
        authUrl += '&pushPayload=' + encodeURI(this.pushPayload)
      }

      let contextMsg = this.authContextMsg;
      let contextMsgBase64 = self.utoa(contextMsg);
      authUrl = authUrl + '&preContextTitle=' + contextMsgBase64;

      const authResponse = await fetch(authUrl)  ;
      if (authResponse.ok) {
        setTimeout(function() {self.loopCheckStatus(servicePath, "/authorizepayment");}, 3000);
      } else {
        const resText = await authResponse.text();
        await self.reportError("authorizepayment", JSON.stringify(JSON.parse(resText), null, 2))
      }
    },
    getDevices : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      const devicesResponse = await fetch('/mobileid-inapp' + servicePath +
          '/authorizepayment/getDevices?externalRef='+ extRef )  ;
      if (devicesResponse.ok) {
        const jsonObject = await devicesResponse.json()
        // --- Clean previously listed devices
        this.deviceList = [];
        for( var i = 0; i < jsonObject.length; i++ ) {
          this.deviceList.push({ index: i, value: jsonObject[i]})
        }
      this.selectedDevice = jsonObject[0];
      } else {
        const resText = await devicesResponse.text();
        await self.reportError("authorizepayment", resText)
      }
    },
    loopCheckStatus: async function(servicePath, oper) {
      let self = this;
      const statusResult = await self.checkStatus(servicePath, oper);
      console.log("CheckStatus:" + statusResult)
      if (statusResult === "COMPLETED" || statusResult === "finished") {
        console.log("Stop checking result")
        await self.doComplete(servicePath, oper)
      }
      else {
        setTimeout(function() {self.loopCheckStatus(servicePath, oper);}, 3000);
      }
    },
    checkStatus: async function(servicePath, oper) {
      const statusResponse = await fetch('/mobileid-inapp' + servicePath + oper + '/checkStatus');
      if (statusResponse.ok) {
        const statusResult = await statusResponse.text();
        return statusResult;
      }
      else {
        throw Error(statusResponse.statusText);
      }
    },
    doComplete: async function(servicePath, oper) {
      const completeResponse = await fetch('/mobileid-inapp' + servicePath + oper + '/doComplete');
      if (completeResponse.ok) {
        const jsonObject = await completeResponse.json();
        this.response = JSON.stringify(jsonObject, null, 2);
      }
      else {
        throw Error(completeResponse.statusText);
      }
    },
    reportError: async function(oper, message) {
      this.response = message;
    },
    utoa: function (data) {
      return btoa(unescape(encodeURIComponent(data)));
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
