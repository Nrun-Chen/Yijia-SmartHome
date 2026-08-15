import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    token: '',
  }),
  actions: {
    login(userData) {
      this.username = userData.username
      this.token = userData.token
    },
    logout() {
      this.username = ''
      this.token = ''
    },
  },
})
