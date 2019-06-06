<template>
	<div>
	<v-dialog
		v-if="show"
		v-model="show"
		persistent
		max-width="650"
	>
		<v-card>
			<v-card-title class="headline" v-html="'다운로드 상태 보기'"></v-card-title>
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
			<v-card-actions>
				<v-spacer></v-spacer>
				<v-btn
					color="primary"
					flat="flat"
					@click="close"
				>
					닫기
				</v-btn>
			</v-card-actions>
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
        this.intervalObj = setInterval(() => {
          stompClient.send('/app/rate/list', {}, {})
        }, 1000)
      } else {
        clearInterval(this.intervalObj)
      }
    }
  },
  data () {
    return {
      dialog: false,
      intervalObj: '',
      headers: [
        { text: '제목', value: 'title', sortable: false },
        { text: '다운로드 경로', value: 'downloadPath', sortable: false },
        { text: '진행율 (%)', value: 'percentDone', sortable: false },
        { text: '동작', value: 'title', sortable: false }
      ]
    }
  },
  beforeMount () {
    // stompClient.connect({}, frame => {
    if (stompClient.connected === true) {
      this.subscribe()
    } else {
      stompClient.connect({}, frame => {
        this.subscribe()
      })
    }
    // })
  },
  // mounted () {
  //   this.intervalObj = setInterval(() => {
  //     stompClient.send('/app/rate/list', {}, {})
  //   }, 1000)
  // },
  methods: {
    subscribe: function () {
      stompClient.subscribe('/topic/rate/list', frame => {
        console.log(frame)
        this.$store.commit('setting/setDownloadStatus', JSON.parse(frame.body))
      }, error => {
        console.error(error)
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
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          this.$store.commit('snackbar/show', msg)
        })
      }
    }
  }
}
</script>
