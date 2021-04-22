<template>
  <div> <!-- Div auto removed when rendered - cannot use for page ID -->
    <div id="registrationRoutePage"/>
    <p class="header-description">MobileID Sample</p>
    <h1>Web to merchant app integration</h1>
    <h2>Registration</h2>
    <h3>
      <span>1 - Enter the <code>externalRef</code> and <code>deviceName</code></span>
      <a tabindex="0" class="question-mark-button" id="Open help section about externalRef and deviceName" @click="$showTip($event, 'show_hide_before_registration')"></a>
    </h3>

    <div  class="info-text-box" id="show_hide_before_registration">
      <p>
      <ul>
        <li>Normally, in order to carry out device activation, the user must already be authenticated by the merchant backend</li>
        <li>Obtaining the <code>externalRef</code> and <code>deviceName</code> is done by the merchant backend</li>
        <li>However, for this particular sample, we make it possible to configure these parameters here.</li>
        <br>
        <li>Note: The default <code>externalRef</code> and <code>deviceName</code> are configured in the <code>application.yaml</code> file.</li>
      </ul>
      </p>
    </div>

    <p><label>External reference</label></p>
    <input v-model="externalRef" id="externalRef" type="small-text-box" aria-label="External reference"/>
    <p><label>Device name</label></p>
    <input v-model="deviceName" id="deviceName" type="small-text-box" aria-label="Device name"/>
    <br>

    <h3>
      <span>2 - Click the <b>Activate device</b> button</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about device activation" @click="$showTip($event, 'show_hide_register_device')"></a>
    </h3>

    <div id="show_hide_register_device" class="info-text-box">
      <p>
      <ul>
        <li>The client sends the registration request to the sample backend (<code>startRegistration</code>)</li>
        <ul>
          <li>Note: The configured <code>externalRef</code> and <code>deviceName</code> are sent in the request, while this would normally not be the case, as noted above</li>
        </ul>
        <li>Sample backend</li>
          <ul>
            <li>Collects all necessary data and generates <code>authorizationUrl</code></li>
            <li>Initiates authorization code grant flow towards Signicat</li>
            <li>Sends response back to the client</li>
            <ul>
              <li><code>activationCode</code> (displayed in the text field below)</li>
              <li><code>statusUrl</code> and <code>completeUrl</code> links</li>
            </ul>
          </ul>
      </ul>
      </p>
    </div>
    <div> <a tabindex="0" class="button" @click="startRegistration">Activate device</a></div>

    <p>
      <label>Activation code</label>
    </p>
    <input v-model="activationCode" id="pairingCode" :type="activationCode.length > 6 ? 'large-text-box' : 'small-text-box'" readonly="readonly"
           placeholder="Received code" aria-label="Activation code"/>
    <br>

    <h3>
      <span>3 - Use mobile app and enter activation code to activate MobileID on your device</span>
      <a tabindex="0" class="question-mark-button" id="Open help section about the activation flow" @click="$showTip($event, 'show_hide_use_app')"></a>
    </h3>
    <div  class="info-text-box" id="show_hide_use_app">
      <p>
      <ul>
        <li>The client executes polling calls to the sample backend's <code>/register/checkStatus</code> endpoint</li>
      <ul><li>The sample backend uses the received <code>statusUrl</code> and executes a call to Signicat</li></ul>
        <li>When the device activation is fulfilled, the client calls the sample backend's <code>/register/doComplete</code>
          endpoint</li>
          <ul><li>The sample backend executes a call to the <code>completeUrl</code></li></ul>
        <li>Signicat answers with the <code>authorizationCode</code> to the <code>redirect_uri</code></li>
        <li>The sample backend carries out regular token exchange and fetches <code>userinfo</code></li>
        <li>The result is shown in the field below</li>
      </ul>
      </p>
    </div>

    <p>
      <label>Registration response</label>
    </p>
    <textarea v-model="response" disabled="disabled" aria-label="Registration response"></textarea>

  </div>
</template>


<script>
export default {
  name : 'Registration',
  data() {
    return {
      externalRef : "",
      deviceName : "",
      activationCode : "",
      response : "",
      servicePath : this.$store.state.servicePath
    }
  },
  beforeMount() {
    this.init();
  },
  methods: {
    init: async function() {
      const initResponse = await fetch('/mobileid-inapp' + this.servicePath +'/register/init')  ;
      if (initResponse.ok) {
        const jsonObject = await initResponse.json();
        this.externalRef = jsonObject.externalRef;
        this.deviceName = jsonObject.deviceName;
      } else {
        throw Error(initResponse.statusText);
      }
    },
    startRegistration: async function() {
      let self = this;
      let extRef = this.externalRef
      let devName = this.deviceName
      let servicePath = this.servicePath
      const regResponse = await fetch('/mobileid-inapp' + servicePath + '/register/start?externalRef='+ extRef +
          '&deviceName='
          + devName);
      const resText = await regResponse.text();
      if (regResponse.ok) {
        this.activationCode = resText;
        setTimeout(function() {self.loopCheckStatus(servicePath, "/register");}, 3000);
      }
      else {
        await self.reportError("register", resText)
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
    }
  }
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
</style>
