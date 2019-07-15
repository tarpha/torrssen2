export const state = () => ({
  show: false,
  result: false,
  auto: false,
  text: '',
  path: [],
  data: {},
  index: -1,
  toggle: false,
  download: {
    active: false,
    done: false,
    stop: false,
    vueIndex: -1,
    id: 0
  }
})

export const mutations = {
  show (state, obj) {
    state.show = true
    state.result = false
    state.auto = false
    state.text = ''
    state.data = obj.data
    state.path = obj.path
    state.index = obj.index
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
  },
  toggle (state, obj) {
    state.toggle = !state.toggle
    state.download.active = obj.active
    state.download.stop = obj.stop
    state.download.vueIndex = obj.vueIndex
    state.download.id = obj.id
    state.download.done = obj.done
  }
}
