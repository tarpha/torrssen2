<template>
  <div>
    <nuxt-download-dialog />
    <v-snackbar
      v-model="show"
      :timeout=3500
      top
    >
      {{ this.$store.state.snackbar.text }}
      <v-btn
        color="pink"
        flat
        @click="$store.commit('snackbar/setShow', false)"
      >
        Close
      </v-btn>
    </v-snackbar>
    <v-layout row>
      <v-flex xs12 sm6 offset-sm3>
        <v-card flat>
          <v-list three-line>
            <template v-for="(item, index) in items">
              <nuxt-feed :item="item" :index="index" :stomp="stomps[index]" :key="index" />
            </template>
          </v-list>
        </v-card>
        <v-alert
          v-if="items.length === 0"
          :value="true"
          type="info"
        >
          검색 결과가 없습니다.
        </v-alert>
        <div xs12 sm4 text-xs-center style="text-align:center">
          <v-btn 
            flat
            :loading="loading"
            :disabled="loading"
            @click="next()"
          >
            더 보기
          </v-btn>
        </div>
      </v-flex>
    </v-layout>
  </div>
</template>

<script>
  import axios from '~/plugins/axios'
  import stompClient from '~/plugins/stomp'
  import NuxtFeed from '~/components/Feed'
  import NuxtDownloadDialog from '~/components/DownloadDialog'

  export default {
    components: {
      NuxtFeed,
      NuxtDownloadDialog
    },
    computed: {
      show: {
        get () {
          return this.$store.state.snackbar.show
        },
        set (value) {
          this.$store.commit('snackbar/setShow', value)
        }
      },
      toggle () {
        return this.$store.state.toolbar.toggle
      },
      downloadToggle () {
        return this.$store.state.download.toggle
      },
      download () {
        return this.$store.state.download.download
      }
    },
    watch: {
      toggle: function (val) {
        this.executeToggle()
      },
      downloadToggle: function (val) {
        this.stomps[this.download.vueIndex].active = this.download.active
        this.stomps[this.download.vueIndex].stop = this.download.stop
        this.stomps[this.download.vueIndex].id = this.download.id
      }
    },
    mounted () {
      axios.get('/api/setting/INIT').then(res => {
        if (res.data !== 'TRUE') {
          this.$store.commit('setting/setShowSetting', true)
        }
      })
      axios.get('/api/setting/DARK_THEME').then(res => {
        this.$store.commit('setDark', res.data === 'TRUE')
      })
      if (stompClient.connected() === true) {
        this.subscription = this.subscribe()
      } else {
        this.intervalObj = setInterval(() => {
          if (stompClient.connected() === false) {
            if (typeof this.subscription.unsubscribe === 'function') {
              this.subscription.unsubscribe()
            }
          }
          if (stompClient.connected() === true) {
            this.subscription = this.subscribe()
            clearInterval(this.intervalObj)
          }
        }, 1000)
      }
    },
    data () {
      return {
        intervalObj: '',
        subscription: '',
        loading: false,
        funcName: 'list',
        page: 0,
        size: 25
      }
    },
    async asyncData ({ app }) {
      const res = await axios.get('/api/rss/feed/list', {
        params: {
          page: 0,
          size: 25,
          sort: 'createDt,desc'
        }
      })
      let stomps = []
      for (var i = 0; i < res.data.content.length; i++) {
        const obj = res.data.content[i]
        stomps.push({
          percentDone: obj.downloading === true ? 0 : -1,
          active: obj.downloading === true,
          stop: false,
          delete: false,
          id: obj.downloading === true ? obj.downloadId : 0
        })
      }
      return {
        items: res.data.content,
        stomps: stomps
      }
    },
    methods: {
      next: function () {
        this.loading = true
        axios.get('/api/rss/feed/' + this.funcName, {
          params: {
            title: this.$store.state.toolbar.searchText,
            page: ++this.page,
            size: this.size,
            sort: 'createDt,desc'
          }
        }).then(res => {
          for (var i = 0; i < res.data.content.length; i++) {
            const obj = res.data.content[i]
            this.stomps.push({
              percentDone: obj.downloading === true ? 0 : -1,
              active: obj.downloading === true,
              stop: false,
              delete: false,
              id: obj.downloading === true ? obj.downloadId : 0
            })
            this.items.push(obj)
          }
          this.loading = false
          if (this.page >= res.data.totalPages) {
            this.page = res.data.totalPages
            this.$store.commit('snackbar/show', 'End of Page')
          }
        })
      },
      subscribe: function () {
        return stompClient.subscribe('/topic/feed/update', frame => {
          if (frame.body === 'true') {
            this.executeToggle()
          }
        })
      },
      executeToggle: function () {
        this.page = 0
        this.funcName = 'list'
        if (this.$store.state.toolbar.searchText) {
          this.funcName = 'search'
        }
        axios.get('/api/rss/feed/' + this.funcName, {
          params: {
            title: this.$store.state.toolbar.searchText,
            page: this.page,
            size: this.size,
            sort: 'createDt,desc'
          }
        }).then(res => {
          this.items = res.data.content
          let stomps = []
          for (var i = 0; i < res.data.content.length; i++) {
            const obj = res.data.content[i]
            stomps.push({
              percentDone: obj.downloading === true ? 0 : -1,
              active: obj.downloading === true,
              stop: false,
              delete: false,
              id: obj.downloading === true ? obj.downloadId : 0
            })
          }
          this.stomps = stomps
        })
      }
    }
  }
</script>
