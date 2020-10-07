import Vue from 'vue'
import App from './App.vue'
import Router from "vue-router";
import Disclaimer from './components/Disclaimer.vue'
import Registration from './components/Registration.vue'
import Authentication from './components/Authentication.vue'
import ConsentSign from './components/ConsentSign.vue'

Vue.use(Router);

Vue.config.productionTip = false

Vue.prototype.$showTip = function(e, id) {
  console.log(e)
  console.log(id)
  var targetElement = document.getElementById(id);
  if (e.target.classList.contains('active')) {
    targetElement.classList.remove('show')
    e.target.classList.remove('active')
  } else {
    targetElement.classList.add('show')
    e.target.classList.add('active')
  }
}

const routes = [
  {
    path: '/mobileid-inapp/',
    component: Disclaimer
  },
  {
    path: '/mobileid-inapp/registration',
    component: Registration
  },
  {
    path: '/mobileid-inapp/authentication',
    component: Authentication
  },
  {
    path: '/mobileid-inapp/consent-sign',
    component: ConsentSign
  }
]

const router = new Router({
  mode: 'history',
  linkActiveClass: 'active',
  routes
});

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
