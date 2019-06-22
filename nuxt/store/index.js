export const state = () => ({
  input: '',
  modalWidth: 650,
  version: '',
  dark: false
})

export const mutations = {
  setInput: function (state, input) {
    state.input = input
  },
  setDark: function (state, val) {
    state.dark = val
  },
  setVesrion: function (state, val) {
    state.version = val
  }
}
