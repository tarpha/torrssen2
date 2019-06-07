<template>
	<div>
	<v-dialog
		v-if="show"
		v-model="show"
		persistent
		max-width="650"
    :fullscreen="windowWidth < 400"
	>
		<v-card>
      <v-toolbar flat>
        <v-btn icon @click="close">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title>다운로드 상태 보기</v-toolbar-title>
      </v-toolbar>
			<!-- <v-card-title class="headline" v-html="'다운로드 상태 보기'"></v-card-title> -->
				<v-data-table
					:headers="headers"
					:items="downloadStatus"
					rows-per-page-text=""
				>
					<template v-slot:items="props">
						<td>{{ props.item.name }}</td>
						<td>{{ props.item.downloadPath }}</td>
						<td>{{ props.item.percentDone }}</td>
						<td class="justify-center layout px-0">
							<v-icon
								small
								@click="deleteItem(props.item.id)"
							>
								delete
							</v-icon>
						</td>
					</template>
				</v-data-table>
			<!-- <v-card-actions>
				<v-spacer></v-spacer>
				<v-btn
					color="primary"
					flat="flat"
					@click="close"
				>
					닫기
				</v-btn>
			</v-card-actions> -->
		</v-card>
	</v-dialog>
	</div>
</template>

<script>
import axios from '~/plugins/axios'
import stompClient from '~/plugins/stomp'

export default {
  computed: {
    show: {
      get () {
        return this.$store.state.setting.showDownloadStatus
      },
      set (value) {
        this.$store.commit('setting/setShowDownloadStatus', value)
      }
    },
    downloadStatus () {
      return this.$store.state.setting.downloadStatus
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        if (stompClient.connected() === true) {
          this.subscription = this.subscribe()
        }
        this.intervalObj = setInterval(() => {
          if (stompClient.connected() === false) {
            this.subscription.unsubscribe()
          }
          if (stompClient.connected() === true) {
            if (this.count < 3) {
              stompClient.publish('/app/rate/list', '')
              this.count++
            } else {
              this.subscription = this.subscribe()
              this.count = 0
            }
          }
        }, 1000)
      } else {
        this.subscription.unsubscribe()
        clearInterval(this.intervalObj)
      }
    }
  },
  data () {
    return {
      dialog: false,
      intervalObj: '',
      subscription: '',
      windowWidth: 0,
      count: 0,
      headers: [
        { text: '제목', value: 'title', sortable: false },
        { text: '다운로드 경로', value: 'downloadPath', sortable: false },
        { text: '진행율 (%)', value: 'percentDone', sortable: false },
        { text: '동작', value: 'title', sortable: false }
      ]
    }
  },
  mounted () {
    this.windowWidth = window.innerWidth
  },
  beforeDestroy () {
    this.subscription.unsubscribe()
    clearInterval(this.intervalObj)
  },
  methods: {
    subscribe: function () {
      return stompClient.subscribe('/topic/rate/list', frame => {
        this.count--
        this.$store.commit('setting/setDownloadStatus', JSON.parse(frame.body))
      })
    },
    close: function () {
      this.editedIndex = -1
      this.regexShow = false
      clearInterval(this.intervalObj)
      this.$store.commit('setting/setShowDownloadStatus', false)
    },
    deleteItem: function (id) {
      if (confirm('이 항목을 삭제하시겠습니까?')) {
        axios.post('/api/download/remove', { 'id': id }).then(res => {
          let msg = '삭제되었습니다.'
          if (res.status !== 200 || res.data === -1) {
            msg = '삭제하지 못했습니다.'
          }
          this.$store.commit('snackbar/show', msg)

          if (res.data >= 0) {
            this.$store.commit('download/toggle', {
              active: true,
              stop: true,
              vueIndex: res.data,
              id: 0
            })
          }
        })
      }
    }
  }
}
</script>
