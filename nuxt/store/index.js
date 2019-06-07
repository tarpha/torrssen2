export const state = () => ({
  input: '',
  dark: false
})

export const mutations = {
  setInput: function (state, input) {
    state.input = input
  },
  setDark: function (state, val) {
    state.dark = val
  }
}
