<template>
  <div>
    <nuxt-setting-dialog/>
    <nuxt-rss-list-dialog />
    <nuxt-download-path-dialog />
    <nuxt-watch-list-dialog />
    <nuxt-seen-list-dialog />
    <nuxt-download-list-dialog />
    <nuxt-download-status-dialog />
    <v-toolbar flat app fixed class="flex xs12 sm6 offset-sm3">
      <v-text-field
        v-model="searchText"
        hide-details
        prepend-inner-icon="search"
        :background-color="dark !== true ? '#f5f5f5' : '#333333'"
        solo
        clearable
        flat
        @keyup.enter="submit"
        @click:clear="clear"
      ></v-text-field>
     <v-menu offset-y :close-on-content-click="false">
        <template v-slot:activator="{ on }">
          <v-btn
            icon
            v-on="on"
          >
            <v-icon>rss_feed</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-tile
            v-for="(item, index) in rss"
            :key="index"
          >
            <v-list-tile-content>
              <v-checkbox
                style="padding-top: 0 !important;"
                v-model="rssList" 
                :label="item.name" 
                :value="item.name" 
                color="primary"
              ></v-checkbox>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile>
            <v-list-tile-action>
          <v-btn
                color="primary"
                flat="flat"
                @click="reload"
              >
                RSS 갱신
              </v-btn>
            </v-list-tile-action>
          </v-list-tile>
        </v-list>
      </v-menu>
      <v-menu offset-y>
        <template v-slot:activator="{ on }">
          <v-btn
            icon
            v-on="on"
          >
            <v-icon>more_vert</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-tile
            v-for="(item, index) in items"
            avatar
            :key="index"
            @click="item.method"
          >
            <v-list-tile-avatar>
              <v-icon>{{ item.icon }}</v-icon>
            </v-list-tile-avatar>
            <v-list-tile-content>
              <v-list-tile-title>{{ item.title }}</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile>
            <v-list-tile-content>
              <v-list-tile-sub-title style="text-align: right">
                {{ $store.state.version }}
              </v-list-tile-sub-title>
            </v-list-tile-content>
          </v-list-tile>

        </v-list>
      </v-menu>
    </v-toolbar>    
  </div>
</template>

<script>
import axios from '~/plugins/axios'
import NuxtSettingDialog from '~/components/SettingDialog'
import NuxtRssListDialog from '~/components/RssListDialog'
import NuxtDownloadPathDialog from '~/components/DownloadPathDialog'
import NuxtWatchListDialog from '~/components/WatchListDialog'
import NuxtSeenListDialog from '~/components/SeenListDialog'
import NuxtDownloadListDialog from '~/components/DownloadListDialog'
import NuxtDownloadStatusDialog from '~/components/DownloadStatusDialog'

export default {
  components: {
    NuxtSettingDialog,
    NuxtRssListDialog,
    NuxtDownloadPathDialog,
    NuxtWatchListDialog,
    NuxtSeenListDialog,
    NuxtDownloadListDialog,
    NuxtDownloadStatusDialog
  },
  data () {
    return {
      rss: [],
      items: [
        {
          title: '환경 설정',
          icon: 'settings',
          method: () => this.$store.commit('setting/setShowSetting', true)
        },
        {
          title: 'RSS 사이트 관리',
          icon: 'rss_feed',
          method: () => this.$store.commit('setting/setShowRssList', true)
        },
        // {
        //   title: 'RSS Feed 관리',
        //   icon: 'list',
        //   method: () => this.$store.commit('setting/setShowRssFeed', true)
        // },
        {
          title: '자동 다운로드 관리',
          icon: 'playlist_add',
          method: () => this.$store.commit('setting/setShowWatchList', true)
        },
        {
          title: '자동 다운로드 이력',
          icon: 'remove_red_eye',
          method: () => this.$store.commit('setting/setShowSeenList', true)
        },
        {
          title: '다운로드 경로 관리',
          icon: 'folder',
          method: () => this.$store.commit('setting/setShowDownloadPath', true)
        },
        {
          title: '다운로드 상태 보기',
          icon: 'playlist_play',
          method: () => {
            this.$store.commit('setting/setShowDownloadStatus', true)
          }
        },
        {
          title: '다운로드 이력 관리',
          icon: 'playlist_add_check',
          method: () => this.$store.commit('setting/setShowDownloadList', true)
        }
      ]
    }
  },
  computed: {
    searchText: {
      get () {
        return this.$store.state.toolbar.searchText
      },
      set (value) {
        this.$store.commit('toolbar/setSearchText', value)
      }
    },
    rssList: {
      get () {
        return this.$store.state.setting.rssList
      },
      set (array) {
        this.$store.commit('setting/setRssList', array)
      }
    },
    dark: function () {
      return this.$store.state.dark
    }
  },
  watch: {
    rssList: {
      handler: function (array) {
        let showList = JSON.parse(JSON.stringify(this.rss))
        for (let i = 0; i < showList.length; i++) {
          showList[i].show = false
          for (let j = 0; j < this.rssList.length; j++) {
            if (showList[i].name === this.rssList[j]) {
              showList[i].show = true
              break
            }
          }
        }
        axios.post('/api/setting/rss-list', showList).then(res => {
          if (res.status !== 200) {
            this.$store.commit('snackbar/show', 'RSS 리스트 설정 중 오류가 발생하였습니다.')
          }
          this.$store.commit('toolbar/toggle')
        })
      },
      deep: true
    }
  },
  mounted () {
    axios.get('/api/setting/rss-list').then(res => {
      this.rss = res.data
      let list = []
      for (let i = 0; i < res.data.length; i++) {
        if (res.data[i].show === true) {
          list.push(res.data[i].name)
        }
      }
      this.$store.commit('setting/setRssList', list)
    })
  },
  methods: {
    submit: function () {
      this.$store.commit('toolbar/toggle')
    },
    clear: function () {
      this.$store.commit('toolbar/toggle')
    },
    reload: function () {
      axios.post('/api/rss/reload', {}).then(res => {
        let msg = '갱신 요청하였습니다.'
        if (res.status !== 200) {
          msg = '갱신 요청에 실패하였습니다.'
        }
        this.$store.commit('snackbar/show', msg)
      })
    }
  }
}
</script>
