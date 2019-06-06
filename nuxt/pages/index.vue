<template>
  <div>
    <nuxt-download-dialog />
    <v-snackbar
      v-model="show"
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
        <div xs12 sm4 text-xs-center style="text-align:center">
        <v-btn 
          flat
          :loading="loading"
          :disabled="loading"
          @click="next()"
        >
          MORE
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
      }
    },
    watch: {
      toggle: function (val) {
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
            stomps.push({
              percentDone: -1,
              active: false,
              delete: false,
              id: 0
            })
          }
          this.stomps = stomps
        })
      }
    },
    beforeMount () {
      stompClient.connect({}, frame => {
        stompClient.subscribe('/topic/rate', frame => {
          const body = JSON.parse(frame.body)
          const vueIndex = body.vueItemIndex
          this.stomps[vueIndex].active = !body.done
          this.stomps[vueIndex].percentDone = body.percentDone
          this.stomps[vueIndex].id = body.id
          this.stomps[vueIndex].delete = false
        }, error => {
          console.error(error)
        })
        stompClient.subscribe('/topic/remove', frame => {
          const body = JSON.parse(frame.body)
          const vueIndex = body.vueItemIndex
          this.stomps[vueIndex].active = false
          this.stomps[vueIndex].delete = true
        }, error => {
          console.error(error)
        })
        stompClient.subscribe('/topic/rate/list', frame => {
          console.log(frame)
          this.$store.commit('setting/setDownloadStatus', JSON.parse(frame.body))
        }, error => {
          console.error(error)
        })
      }, err => { console.log(err) })
    },
    mounted () {
      axios.get('/api/setting/INIT').then(res => {
        if (res.data !== 'TRUE') {
          this.$store.commit('setting/setShowSetting', true)
        }
      })
    },
    data () {
      return {
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
        stomps.push({
          percentDone: -1,
          active: false,
          delete: false,
          id: 0
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
            this.stomps.push({
              percentDone: -1,
              active: false,
              delete: false,
              id: 0
            })
            this.items.push(res.data.content[i])
          }
          this.loading = false
          if (this.page >= res.data.totalPages) {
            this.page = res.data.totalPages
            this.$store.commit('snackbar/show', 'End of Page')
          }
        })
      }
    }
  }
</script>
