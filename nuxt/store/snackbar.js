export const state = () => ({
  show: false,
  text: ''
})

export const mutations = {
  show (state, text) {
    state.show = true
    state.text = text
  },
  setShow (state, value) {
    state.show = value
  }
}
