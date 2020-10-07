<template>
  <div>
    <p class="header-description">MobileID Sample</p>
    <h1>Web to merchant app integration</h1>
    <h2>Authentication</h2>
    <h3>
      <span>1 - Enter the externalRef of the user you want to authenticate</span>
      <a class="question-mark-button" @click="$showTip($event, 'show_hide_basic_info')"></a>
    </h3>
    <div id="show_hide_basic_info" class="info-text-box">
      <p>
      <ul><li>The 'last used' externalRef is suggested</li></ul>
      </p>
    </div>

    <p>
      <label>External Reference</label>
    </p>
    <input v-model="externalRef" type='large-text' value=""/>


    <br>

    <h3>
      <span>2 - Click the <b>Get available devices</b> button</span>
      <a class="question-mark-button" @click="$showTip($event, 'show_hide_get_devices')"></a>
    </h3>
    <div id="show_hide_get_devices" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the request to the sample backend (getDevices) along with the externalRef</li>
          <li>The sample backend fetches all devices (of type MOBILEID) that are registered for the chosen externalRef</li>
          <ul><li>SOAP request <code>getDevices</code> to Signicat</li></ul>
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
    <select v-model="selected" class="signicat-select" text="Choose a device to Authenticate">
      <option v-for="device in deviceList" v-bind:key="device.index"  v-bind:value="device.value">
        {{ device.value }}
      </option>
    </select>

    <br>
    <br>

    <h3>
      <span>3 - Select an authentication device and click the <b>Authenticate</b> button</span>
      <a class="question-mark-button" @click="$showTip($event, 'show_hide_select_device')"></a>
    </h3>
    <div id="show_hide_select_device" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the authentication request to the sample backend (startAuthentication)</li>
        <li>Sample backend</li>
          <ul>
            <li>Obtains <code>deviceId</code> for the selected device name</li>
            <li>Collects all necessary data and generates authorizationUrl</li>
            <li>Initiates authorization code grant flow toward Signicat</li>
            <li>Sends response back to the client (with <code>statusUrl</code> and <code>completeUrl</code> links)</li>
          </ul>
      </ul>
      </p>
    </div>

    <div>
      <button class="button" @click="startAuthentication">Authenticate</button>
    </div>

    <br>
    <br>
    <h3>
      <span>4 - Push notification is displayed on mobile device. Carry out authentication</span>
      <a class="question-mark-button" @click="$showTip($event, 'show_hide_push_info')"></a>
    </h3>
    <div id="show_hide_push_info" class="info-text-box">
      <p>
      <ul >
        <li>The client executes polling calls to the sample backend's
          <code>/authenticate/checkStatus</code> endpoint</li>
      <ul><li>The sample backend uses the received <code>statusUrl</code> and executes a call to Signicat</li></ul>
      <li>When authentication is fulfilled, the client calls the sample backend's <code>/authenticate/doComplete</code>
        endpoint</li>
          <ul><li>The sample backend executes a call to the <code>completeUrl</code></li></ul>
        <li>Signicat answers with the <code>authorizationCode</code> to the <code>redirect_uri</code>.
        <li>The sample backend carries out regular token exchange and fetches <code>userinfo</code></li>
        <li>The result is shown in the field below</li>
      </ul>
      </p>
    </div>

    <p>
      <label>Authentication response</label>
    </p>
    <textarea v-model="response" id="authenticateCompleteResponse" disabled="disabled"></textarea>


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
      selected: '',
      deviceList: [],
      servicePath : "/web"
    }
  },

  beforeMount() {
    this.init()
  },

  methods : {
    init : async function() {
      const initResponse = await fetch('/mobileid-inapp' + this.servicePath +'/authenticate/init')  ;
      if (initResponse.ok) {
        this.externalRef = await initResponse.text();
      } else {
        throw Error(initResponse.statusText);
      }
    },
    startAuthentication : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      let u = this.selected
      let authUrl = '/mobileid-inapp' + servicePath + '/authenticate/start?externalRef='+ extRef +
          '&deviceName='
          + u;
      const authResponse = await fetch(authUrl)  ;
      if (authResponse.ok) {
        setTimeout(function() {self.loopCheckStatus(servicePath, "/authenticate");}, 3000);
      } else {
        const resText = await authResponse.text();
        await self.reportError("authenticate", JSON.stringify(JSON.parse(resText), null, 2))
      }
    },
    getDevices : async function() {
      let self = this;
      let extRef = this.externalRef
      let servicePath = this.servicePath
      const devicesResponse = await fetch('/mobileid-inapp' + servicePath +
          '/authenticate/getDevices?externalRef='+ extRef )  ;
      if (devicesResponse.ok) {
        const jsonObject = await devicesResponse.json()
        // --- Clean previously listed devices
        this.deviceList = [];
        for( var i = 0; i < jsonObject.length; i++ ) {
          this.deviceList.push({ index: i, value: jsonObject[i]})
        }
      this.selected = jsonObject[0];
      } else {
        const resText = await devicesResponse.text();
        await self.reportError("authenticate", resText)
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
