<template>
  <div id="app" class="background-dots">
    <header class="signicat-header sticky">
      <div class="header-wrapper">
        <div class="menu-toggler" @click="toggleMenu">
          <i class="material-icons open-menu-icon" v-if="!menuOpen">menu</i>
          <i class="material-icons close-menu-icon" v-if="menuOpen">close</i>
          <span>Menu</span>
        </div>
        <a class="logo" @click="openSignicatSiteInNewTab">
          <img src="signicat-logo-black.svg" data-bind="attr: {src: logo}" />
        </a>
        <nav v-bind:class="{'slide-in': menuOpen}">
          <h2>MobileID Sample</h2>
          <router-link to="/mobileid-inapp/" exact>Disclaimer</router-link>
          <router-link to="/mobileid-inapp/registration">Registration</router-link>
          <router-link to="/mobileid-inapp/authentication">Authentication</router-link>
          <router-link to="/mobileid-inapp/payment-authorization">Payment authorization</router-link>
          <router-link to="/mobileid-inapp/consent-sign">Consent signature</router-link>
        </nav>
      </div>
    </header>
    <div class="content-wrapper">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>

export default {
  name: "App",
  data() {
    return {
      menuOpen: false
    }
  },
  methods: {
    toggleMenu: function() {
      this.menuOpen = !this.menuOpen;
    },
    openSignicatSiteInNewTab: function () {
      window.open('https://www.signicat.com', '_blank');
    }
  }
};
</script>

<style lang="less">
@BASE_URL: "/mobileid-inapp";

html body {
  .signicat-header {
    background-color: white;
    .menu-toggler {
      color: #3f505f;
    }
  }

  .background-dots {
    background-image: url("@{BASE_URL}/background-image/connecting-dots.png");
    background-position: 100% 100%;
    background-size: 300px;
    min-height: 100%
  }

  h1 {
    margin-top: 0;
  }
  h3 {
    margin-top: 30px;
  }
  h2 + h3 {
    margin-top: 20px;
  }
  p {
    font-size: 16px;
    color: #60768a;
  }
  p.header-description {
    margin-bottom:10px;
    color:#60768a;
    font-size: 16px;
  }
  .content-wrapper {
    max-width: 900px;
    p {
      line-height: normal;
    }
  }
  .question-mark-button {
    width: 30px;
    height: 30px;
    background-image: url("@{BASE_URL}/icons/help-normal.svg");
    background-size: contain;
    background-position: 50% 50%;
    float: right;

    &.active {
      background-image: url("@{BASE_URL}/icons/invalid-name.svg");
    }
  }
  .info-text-box {
    border-radius: 4px;
    background-color: #f0f4f8;
    display: none;
    &.show {
      display: block;
    }
    p {
      color: #3f505f;
      padding: 20px;
    }
  }
  input[type="small-text-box"] {
    max-width:100%;
    width:150px;
    border:0;
    background-color:#f9f9f9;
    padding-top:10px;
    padding-bottom:10px;
    padding-right:5px;
    padding-left:10px;
    border-radius:5px;
    font-size:16px;
    border:2px solid #fafafa;
    margin-top:5px;
    margin-bottom:16px;
    display:block;
    box-shadow:inset 1px 1px 3px rgba(0,0,0,0.2);
    position:relative;transition:.2s
  }
  input[type="medium-text-box"] {
    max-width:100%;
    width:390px;
    border:0;
    background-color:#f9f9f9;
    padding-top:10px;
    padding-bottom:10px;
    padding-right:5px;
    padding-left:10px;
    border-radius:5px;
    font-size:16px;
    border:2px solid #fafafa;
    margin-top:5px;
    margin-bottom:16px;
    display:block;
    box-shadow:inset 1px 1px 3px rgba(0,0,0,0.2);
    position:relative;transition:.2s
  }
  input[type="large-text-box"] {
    max-width:100%;
    width:780px;
    border:0;
    background-color:#f9f9f9;
    padding-top:10px;
    padding-bottom:10px;
    padding-right:5px;
    padding-left:10px;
    border-radius:5px;
    font-size:16px;
    border:2px solid #fafafa;
    margin-top:5px;
    margin-bottom:16px;
    display:block;
    box-shadow:inset 1px 1px 3px rgba(0,0,0,0.2);
    position:relative;transition:.2s
  }
  textarea {
    width:780px;
    height:230px;
    max-width:100%;
    margin-top:5px;
    margin-bottom:10px;
    background-color:#f9f9f9;
  }
  input[type="checkbox"]+label {
    display: inline-block;
    margin-top: 5px;
    margin-left: 15px;
    margin-bottom: 16px;
    font-weight: 600;
  }
}
</style>
