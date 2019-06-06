export const state = () => ({
  searchText: '',
  toggle: false
})

export const mutations = {
  setSearchText (state, value) {
    state.searchText = value
  },
  toggle (state) {
    state.toggle = !state.toggle
  }
}
