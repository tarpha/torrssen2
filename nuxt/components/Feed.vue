<template>
	<div>
		<v-divider
			v-if="index > 0"
			inset
		></v-divider>
		<v-list-tile
			avatar
		>
			<v-list-tile-avatar>
				<img v-if="validURL(item.rssPoster)" :src="item.rssPoster">
        <v-icon v-else x-large>mood</v-icon>
			</v-list-tile-avatar>

			<v-list-tile-content>
				<v-list-tile-title icon>
          <v-icon v-if="item.watch" size="1.2em" color="yellow darken-3">star</v-icon>
          <v-icon v-if="item.downloaded || stomp.done" size="1.2em" color="red darken-3">done</v-icon>
          <v-icon v-if="item.link && !item.link.startsWith('magnet')" size="1.2em">attachment</v-icon>
          {{ item.rssTitle }}
          <v-icon v-if="item.desc && item.desc.startsWith('http')" size="1.2em" @click="openUrl(item.desc)">scatter_plot</v-icon>
        </v-list-tile-title>
				<v-list-tile-sub-title
					v-html="getSubTitle(item)"
				>
        </v-list-tile-sub-title>
				<v-list-tile-sub-title>
          {{ item.rssSite }}
				</v-list-tile-sub-title>
			</v-list-tile-content>

      <v-list-tile-action v-if="stomp.active" >
        <v-icon 
          color="red"
          @click="remove(stomp.id)"
        >
          clear
        </v-icon>
      </v-list-tile-action>
			<v-list-tile-action>
				<v-list-tile-action-text v-if="!stomp.active">{{ getSubTime(item.createDt) }}</v-list-tile-action-text>
				<v-btn 
          v-if="!stomp.active" 
          icon
          @click="downloadShow(item, index)"
        >
					<v-icon 
            :color="dark !== true ? 'blue-grey darken-2' : 'grey lighten-4'"
          >get_app</v-icon>
				</v-btn>
        <nuxt-progress 
          v-else
          :id="stomp.id"
          :index="index"
          :stop="stop"
          :title="item.title"
        />
			</v-list-tile-action>
		</v-list-tile>
	</div>
</template>

<script>
import axios from '~/plugins/axios'
import NuxtProgress from '~/components/Progress'

export default {
  components: {
    NuxtProgress
  },
  props: {
    item: {
      type: Object,
      required: true
    },
    stomp: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: false
    },
    dark: {
      type: Boolean,
      required: true
    }
  },
  data () {
    return {
      intervalObj: '',
      stop: false
    }
  },
  watch: {
    'stomp.stop': function (val) {
      if (val === true) {
        this.stop = true
      }
    }
  },
  methods: {
    downloadShow: function (item, index) {
      this.stop = false
      axios.get('/api/setting/download-path/compute', {
        params: {
          'title': item.rssTitle,
          'season': item.rssSeason
        }
      }).then(res => {
        this.$store.commit('download/show', { data: item, path: res.data, index: index })
      })
    },
    remove: async function (id) {
      axios.post('/api/download/remove', { 'id': id }).then(res => {
        let msg = '삭제: '
        if (res.status !== 200) {
          msg = '삭제 실패: '
        }
        this.stop = true
        this.$store.commit('snackbar/show', msg + this.item.title)
      })
    },
    validURL: function (str) {
      var pattern = new RegExp('^(https?:\\/\\/)?' + // protocol
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // domain name
        '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
        '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
        '(\\#[-a-z\\d_]*)?$', 'i') // fragment locator
      return !!pattern.test(str)
    },
    getSubTitle: function (item) {
      if (item.tvSeries === false) {
        let regex = /\d{3,4}p/i
        let spos = item.rssTitle.search(regex)
        return item.rssTitle.substring(spos)
      } else {
        return '<span class=\'text--primary\'>S' + item.rssSeason +
          ' E' + item.rssEpisode + '</span> &mdash; ' +
          ' ' + (item.rssDate !== null ? item.rssDate : '') +
          ' ' + item.rssQuality + ' ' + item.rssReleaseGroup +
          (item.title.includes('자체자막') ? ' ' + '자체자막' : (item.title.includes('자막') ? ' ' + '자막' : ''))
      }
    },
    getSubTime: function (time) {
      const now = new Date()
      const dt = Date.parse(time.substring(0, 23) + 'Z')
      const diff = now - dt

      if (diff < 60000) {
        return '지금'
      }

      const seconds = parseInt(diff) / 1000
      const minutes = seconds / 60
      const hours = minutes / 60
      const days = hours / 24
      const years = days / 365

      if (parseInt(years) > 0) {
        return parseInt(years) + ' 년전'
      } else if (parseInt(days) > 0) {
        return parseInt(days) + ' 일전'
      } else if (parseInt(hours) > 0) {
        return parseInt(hours) + ' 시간전'
      } else if (parseInt(minutes) > 0) {
        return parseInt(minutes) + ' 분전'
      } else if (parseInt(seconds) > 0) {
        return parseInt(seconds) + ' 초전'
      }
    },
    openUrl: function (url) {
      window.open(url, '_blank')
    }
  }
}
</script>