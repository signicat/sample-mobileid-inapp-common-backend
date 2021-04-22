<template>
  <div id="app" class="background-dots" @click="onClickApp">
    <header class="signicat-header sticky">
      <div class="header-wrapper">
        <div class="menu-toggler" @click="toggleMenu">
          <i class="material-icons open-menu-icon" v-if="!menuOpen">menu</i>
          <i class="material-icons close-menu-icon" v-if="menuOpen">close</i>
          <span>Menu</span>
        </div>
        <a class="logo" @click="openSignicatSiteInNewTab">
          <img src="signicat-logo-black.svg" alt="Open signicat.com in a new tab" data-bind="attr: {src: logo}" />
        </a>
        <nav v-bind:class="{'slide-in': menuOpen}" id="navigator">
          <h2>MobileID Sample</h2>
          <router-link id="disclaimerRoute" to="/mobileid-inapp/" exact>Disclaimer</router-link>
          <router-link id="registrationRoute" to="/mobileid-inapp/registration">Registration</router-link>
          <router-link id="authenticationRoute" to="/mobileid-inapp/authentication">Authentication</router-link>
          <router-link id="paymentRoute" to="/mobileid-inapp/payment-authorization">Payment authorization</router-link>
          <router-link id="consentRoute" to="/mobileid-inapp/consent-sign">Consent signature</router-link>
        </nav>
      </div>
    </header>
    <div class="content-wrapper">
      <router-view id="routerView" ref="routerView"></router-view>
    </div>
  </div>
</template>

<script>

export default {
  name: "App",
  data() {
    return {
      menuOpen: false,
      lastFocusedParent: null,
    }
  },
  mounted() {
    console.log('mounted');
    window.addEventListener('keyup', this.handleKeyUp);
    this.$nextTick(function () { // Code that will run only after the entire view has been rendered
      // Make sure correct element has focus initially
      document.title = this.$route.meta.title;
      this.setFocusWithoutHighlight(document.getElementById(this.$route.name + 'Page'));
    })
  },
  destroyed() {
    console.log('destroyed');
    window.removeEventListener('keyup', this.handleKeyUp)
  },
  watch: {
    $route: function(to) {
      this.$nextTick(function () {
        document.title = to.meta.title; // Change document title - WCAG requires this on navigation change
        setTimeout(() => {
          // Handles focus transfer to 'page' content when clicking a specific route in navigator
          let focusTarget = (this.$refs.routerView.$refs.componentFocusTarget !== undefined)
              ? this.$refs.routerView.$refs.componentFocusTarget
              : this.$refs.routerView.$el;
            this.setFocusWithoutHighlight(focusTarget);
        }, 0);
      });
    },
  },
  methods: {
    focusAndUpdateLastFocusedParent(elem) {
      elem.focus();
      this.lastFocusedParent = document.activeElement.parentElement;
    },
    setFocusWithoutHighlight(elem) {
      // Make focustarget programmatically focussable
      elem.setAttribute('tabindex', '-1');
      this.focusAndUpdateLastFocusedParent(elem);
      // Remove tabindex from focustarget.
      // Reason: https://axesslab.com/skip-links/#update-3-a-comment-from-gov-uk
      elem.removeAttribute('tabindex');
    },
    onClickApp() {
      this.lastFocusedParent = document.activeElement.parentElement;
    },
    handleKeyUp(event) {
      const focused = document.activeElement;

      if (event.key === 'Enter') {
        if (focused) {
          focused.click(); // Click on help questionmarks etc with keyboard
        }

        if (focused.parentElement.id === 'navigator') {
          // Corner case when you use keyboard to move focus back to navigation, then press ENTER on the page that is
          // already "selected" in the navigator - focus should jump into that page (again)
          this.setFocusWithoutHighlight(document.getElementById(focused.id + 'Page'));
        }
      } else if (this.lastFocusedParent && !(this.lastFocusedParent.id === 'navigator')) {
        // We are keyboard navigating "back" to the navigator with, for example SHIFT+TAB, so focus the current
        // route in the navigator.
        const routeInNavigatorHasFocus = focused.parentElement.id === 'navigator';
        if (routeInNavigatorHasFocus && event.key === 'Tab' && event.shiftKey) {
          this.focusAndUpdateLastFocusedParent(document.getElementById(this.$router.currentRoute.name));
        }
      }
    },
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
