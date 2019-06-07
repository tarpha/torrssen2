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
        background-color="#f5f5f5"
        solo
        clearable
        flat
        @keyup.enter="submit"
        @click:clear="clear"
      ></v-text-field>
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
        </v-list>
      </v-menu>
    </v-toolbar>    
  </div>
</template>

<script>
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
    }
  },
  methods: {
    submit: function () {
      this.$store.commit('toolbar/toggle')
    },
    clear: function () {
      this.$store.commit('toolbar/toggle')
    }
  }
}
</script>
