<template>
  <div> <!-- Div auto removed when rendered - cannot use for page ID -->
    <div id="consentRoutePage"/>
    <p class="header-description">MobileID Sample</p>
    <h1>Web to merchant app integration</h1>
    <h2>Consent signature</h2>
    <h3>
      <span>1 - Enter the <code>externalRef</code> of the user you want to authenticate</span>
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
      <option v-for="device in deviceList" v-bind:key="device.index" v-bind:value="device.value">
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
      <span>4 - Enter the consent text and optionally metadata, and click the <b>Sign</b> button</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about consent text" @click="$showTip($event, 'show_hide_select_device')"></a>
    </h3>
    <div id="show_hide_select_device" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the consent signature request to the sample backend</li>
        <li>Sample backend</li>
          <ul>
            <li>Obtains <code>deviceId</code> for the selected device name</li>
            <li>Collects all necessary data and generates <code>authorizationUrl</code> with encrypted request object. Note that the encryption key has to be retrieved from <code>https://&lt;ENV&gt;/oidc/jwks.json</code></li>
            <li>Initiates authorization code grant flow towards Signicat</li>
            <li>Sends response back to the client (with <code>statusUrl</code> and <code>completeUrl</code> links)</li>
          </ul>
      </ul>
      </p>
    </div>

    <p>
      <label>Consent text</label>
      <input v-model="authContextMsg" type='medium-text-box' value="Enter consent sign text" aria-label="Consent text"/>
    </p>

    <p>
      <input type="checkbox" id="metaDataCheck" v-model="checked"/>
      <label for="metaDataCheck"> Specify metadata (optional)</label>
      <input v-model="metaData" type='medium-text-box' value="Enter metadata" placeholder="Metadata" v-if="checked"/>
    </p>

    <div>
      <button class="button" @click="startSigning">Sign</button>
    </div>

    <br>
    <br>

    <h3>
      <span>4 - Push notification is displayed on the mobile device. Carry out authentication</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about push notification" @click="$showTip($event, 'show_hide_push_info')"></a>
    </h3>
    <div id="show_hide_push_info" class="info-text-box">
      <p>
      <ul >
        <li>The client executes polling calls to the sample backend's <code>/consentsign/checkStatus</code> endpoint</li>
      <ul><li>The sample backend uses the received <code>statusUrl</code> and executes a call to Signicat</li></ul>
        <li>When authentication is fulfilled, the client calls the sample backend's <code>/consentsign/doComplete</code> endpoint</li>
          <ul><li>The sample backend executes a call to the <code>completeUrl</code></li></ul>
      <li>Signicat answers with the <code>authorizationCode</code> to the <code>redirect_uri</code></li>
        <li>The sample backend carries out regular token exchange and fetches the signed document via the <code>signature</code> endpoint</li>
        <li>The result is shown in the field below (Base64-decoded signed data)</li>
      </ul>
      </p>
    </div>

    <p>
      <label>Signed response</label>
    </p>
      <textarea v-model="response" id="consentsignCompleteResponse" disabled="disabled" aria-label="Signed response"></textarea>

  </div>
</template>

<script>
export default {
  name: 'Authentication',
  data() {
    return {
      deviceId : "",
      externalRef : "",
      response : "",
      authContextMsg : "",
      metaData : "",
      checked : false,
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
      const initResponse = await fetch('/mobileid-inapp' + this.servicePath +'/consentsign/init')  ;
      if (initResponse.ok) {
        this.externalRef = await initResponse.text();
      } else {
        throw Error(initResponse.statusText);
      }
    },
    startSigning : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      let u = this.selectedDevice
      let authUrl = '/mobileid-inapp' + servicePath + '/consentsign/start?externalRef='+ extRef +
          '&deviceName=' + u;

      if (this.pushPayloadChecked === true) {
        authUrl += '&pushPayload=' + encodeURI(this.pushPayload)
      }

      let contextMsg = this.authContextMsg;
      let contextMsgBase64 = self.utoa(contextMsg);
      authUrl = authUrl + '&preContextTitle=' + contextMsgBase64;

      if(this.checked){
        let metaData = this.metaData;
        authUrl = authUrl + '&preContextContent=' + metaData;
      }

      const authResponse = await fetch(authUrl)  ;
      if (authResponse.ok) {
        setTimeout(function() {self.loopCheckStatus(servicePath, "/consentsign");}, 3000);
      } else {
        const resText = await authResponse.text();
        await self.reportError("consentsign", JSON.stringify(JSON.parse(resText), null, 2))
      }
    },
    getDevices : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      const devicesResponse = await fetch('/mobileid-inapp' + servicePath +
          '/consentsign/getDevices?externalRef='+ extRef )  ;
      if (devicesResponse.ok) {
        const jsonObject = await devicesResponse.json()
        for( var i = 0; i < jsonObject.length; i++ ) {
          this.deviceList.push({ index: i, value: jsonObject[i]})
        }
        this.selectedDevice = jsonObject[0];
      } else {
        const resText = await devicesResponse.text();
        await self.reportError("consentsign", resText)
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
