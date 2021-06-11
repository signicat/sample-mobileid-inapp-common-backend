import Vue from 'vue'
import Vuex from 'vuex'
import App from './App.vue'
import Router from 'vue-router';
import Disclaimer from './components/Disclaimer.vue'
import Registration from './components/Registration.vue'
import Authentication from './components/Authentication.vue'
import PaymentAuthorization from './components/PaymentAuthorization.vue'
import ConsentSign from './components/ConsentSign.vue'

Vue.use(Router);

Vue.use(Vuex)

Vue.config.productionTip = false

Vue.prototype.$showTip = function(e, id) {
  console.log(e);
  const targetElement = document.getElementById(id);
  if (e.target.classList.contains('active')) {
    targetElement.classList.remove('show')
    e.target.classList.remove('active')
  } else {
    targetElement.classList.add('show')
    e.target.classList.add('active')
  }
}

const productTitle = 'MobileID Sample - ';
const routes = [
  {
    name: 'disclaimerRoute',
    path: '/mobileid-inapp/',
    component: Disclaimer,
    meta: { title: productTitle + 'Disclaimer'}
  },
  {
    name: 'registrationRoute',
    path: '/mobileid-inapp/registration',
    component: Registration,
    meta: { title: productTitle + 'Registration'}
  },
  {
    name: 'authenticationRoute',
    path: '/mobileid-inapp/authentication',
    component: Authentication,
    meta: { title: productTitle + 'Authentication'}
  },
  {
    name: 'paymentRoute',
    path: '/mobileid-inapp/payment-authorization',
    component: PaymentAuthorization,
    meta: { title: productTitle + 'Payment authorization'}
  },
  {
    name: 'consentRoute',
    path: '/mobileid-inapp/consent-sign',
    component: ConsentSign,
    meta: { title: productTitle + 'Consent signature'}
  }
]

const router = new Router({
  mode: 'history',
  linkActiveClass: 'active',
  routes
});

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
