export const state = () => ({
  show: false,
  result: false,
  auto: false,
  text: '',
  path: [],
  data: {}
})

export const mutations = {
  show (state, obj) {
    state.show = true
    state.result = false
    state.auto = false
    state.text = ''
    state.data = obj.data
    state.path = obj.path
  },
  setResult (state, value) {
    state.result = value
  },
  setAuto (state, value) {
    state.auto = value
  },
  setText (state, value) {
    state.text = value
  },
  setShow (state, value) {
    state.show = value
  }
}
