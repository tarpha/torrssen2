<template>
	<div>
	<v-dialog
		v-if="show"
		v-model="show"
		persistent
		max-width="650"
	>
		<v-card>
			<v-card-title class="headline" v-html="'다운로드 이력 관리'"></v-card-title>
				<v-data-table
					:headers="headers"
					:items="items"
					rows-per-page-text=""
				>
					<template v-slot:items="props">
						<td>{{ props.item.name }}</td>
						<td>{{ props.item.downloadPath }}</td>
						<td>{{ props.item.done }}</td>
						<td class="justify-center layout px-0">
							<v-icon
								small
								@click="deleteItem(props.item)"
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
				<v-btn
					color="primary"
					flat="flat"
					@click="deleteAll"
				>
					전체 삭제
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
	</div>
</template>

<script>
import axios from '~/plugins/axios'

export default {
  computed: {
    show: {
      get () {
        return this.$store.state.setting.showDownloadList
      },
      set (value) {
        this.$store.commit('setting/setShowDownloadList', value)
      }
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        axios.get('/api/setting/download-list?sort=createDt,desc').then(res => {
          this.items = res.data
        })
      }
    }
  },
  data () {
    return {
      items: [],
      dialog: false,
      intervalObj: '',
      headers: [
        { text: '제목', value: 'title', sortable: false },
        { text: '다운로드 경로', value: 'downloadPath', sortable: false },
        { text: '완료여부', value: 'done', sortable: false },
        { text: '동작', value: 'title', sortable: false }
      ]
    }
  },
  methods: {
    close: function () {
      this.editedIndex = -1
      this.regexShow = false
      this.$store.commit('setting/setShowDownloadList', false)
    },
    deleteItem: function (item) {
      const index = this.items.indexOf(item)
      if (confirm('이 항목을 삭제하시겠습니까?')) {
        axios.post('/api/setting/download-list/delete', item).then(res => {
          let msg = '삭제되었습니다.'
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          this.$delete(this.items, index)
          this.$store.commit('snackbar/show', msg)
        })
      }
    },
    deleteAll: function (item) {
      if (confirm('전체 항목을 삭제하시겠습니까?')) {
        axios.post('/api/setting/download-list/delete/all').then(res => {
          let msg = '삭제되었습니다.'
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          this.items = []
          this.$store.commit('snackbar/show', msg)
        })
      }
    }
  }
}
</script>
