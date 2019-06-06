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
				<v-list-tile-title v-html="item.rssTitle"></v-list-tile-title>
				<v-list-tile-sub-title 
					v-html="getSubTitle(item)"
				></v-list-tile-sub-title>
				<v-list-tile-sub-title v-html="item.rssSite">
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
					<v-icon color="blue-grey darken-2">get_app</v-icon>
				</v-btn>
        <v-progress-circular
          v-else
          :rotate="-90"
          :size="50"
          :width="7"
          :value="stomp.percentDone"
          color="teal"
        >
          {{ stomp.percentDone }}
        </v-progress-circular>
			</v-list-tile-action>
		</v-list-tile>
	</div>
</template>

<script>
import axios from '~/plugins/axios'
import stompClient from '~/plugins/stomp'

export default {
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
    }
  },
  data () {
    return {
      intervalObj: ''
    }
  },
  watch: {
    'stomp.active': function (val) {
      if (val) {
        this.$nextTick(() => {
          this.intervalObj = setInterval(() => {
            stompClient.send('/app/rate', this.stomp.id, {})
          }, 1000)
        })
      } else {
        if (this.intervalObj !== '') {
          clearInterval(this.intervalObj)
        }
        const msg = this.stomp.delete ? 'Delete: ' : 'Complete: '
        this.$store.commit('snackbar/show', msg + this.item.title)
      }
    }
  },
  methods: {
    downloadShow: function (item, index) {
      // const downloadPath = await axios.get('/api/setting/download-path')
      // const seasonPrefix = await axios.get('/api/setting/SEASON_PREFIX')
      // downloadPath.data.forEach(element => {
      //   pathList.push({
      //     path: element.path + (element.useTitle ? '/' + item.rssTitle : '') + (element.useSeason ? '/' + seasonPrefix.data + item.rssSeason : ''),
      //     name: element.name
      //   })
      // })

      axios.get('/api/setting/download-path/compute', {
        params: {
          'title': item.rssTitle,
          'season': item.rssSeason
        }
      }).then(res => {
        item['vueItemIndex'] = index
        this.$store.commit('download/show', { data: item, path: res.data })
      })
    },
    remove: async function (id) {
      stompClient.send('/app/remove', JSON.stringify({
        'id': id,
        'vueItemIndex': this.index
      }), {})
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
      return '<span class=\'text--primary\'>S' + item.rssSeason +
      ' E' + item.rssEpisode + '</span> &mdash; ' +
      item.rssQuality + ' ' + item.rssReleaseGroup
    },
    getSubTime: function (time) {
      const now = new Date()
      const dt = new Date(time)
      const diff = now - dt
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
    }
  }
}
</script>