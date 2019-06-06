export const state = () => ({
  showSetting: false,
  showRssList: false,
  showDownloadPath: false,
  showWatchList: false,
  showSeenList: false,
  showDownloadList: false,
  showDownloadStatus: false,
  downloadStatus: []
})

export const mutations = {
  setShowSetting (state, value) {
    state.showSetting = value
  },
  setShowRssList (state, value) {
    state.showRssList = value
  },
  setShowDownloadPath (state, value) {
    state.showDownloadPath = value
  },
  setShowWatchList (state, value) {
    state.showWatchList = value
  },
  setShowSeenList (state, value) {
    state.showSeenList = value
  },
  setShowDownloadList (state, value) {
    state.showDownloadList = value
  },
  setShowDownloadStatus (state, value) {
    state.showDownloadStatus = value
  },
  setDownloadStatus (state, array) {
    state.downloadStatus = array
  }
}
