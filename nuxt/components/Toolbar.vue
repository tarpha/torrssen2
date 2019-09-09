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
        :background-color="dark ? '#333333' : '#f5f5f5'"
        solo
        clearable
        flat
        @keyup.enter="submit"
        @click:clear="clear"
      ></v-text-field>
     <v-menu offset-y :close-on-content-click="false" max-height="83%">
        <template v-slot:activator="{ on }">
          <v-btn
            icon
            v-on="on"
            @click="loadRssList"
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
          <v-list-tile avartar @click="openGithub">
            <v-list-tile-avatar>
              <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
            </v-list-tile-avatar>
            <v-list-tile-content>
              <v-list-tile-sub-title>
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
  props: {
    dark: {
      type: Boolean,
      required: true
    }
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
    },
    loadRssList: function () {
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
    openGithub: function () {
      window.open('https://github.com/tarpha/torrssen2', '_blank')
    }
  }
}
</script>
