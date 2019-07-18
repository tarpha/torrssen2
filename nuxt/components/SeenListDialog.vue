<template>
	<div>
	<v-dialog
		v-if="show"
		v-model="show"
		persistent
		:max-width="$store.state.modalWidth"
    :fullscreen="windowWidth < $store.state.modalWidth"
	>
		<v-card>
      <v-toolbar flat extended>
        <v-btn icon @click="close">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title>자동 다운로드 이력</v-toolbar-title>
        <template v-slot:extension>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            flat="flat"
            @click="deleteAll"
          >
            전체 삭제
          </v-btn>
        </template>
      </v-toolbar>
				<v-data-table
					:headers="headers"
					:items="items"
					rows-per-page-text=""
				>
					<template v-slot:items="props">
            <td>
							<v-icon
								small
								@click="deleteItem(props.item)"
							>
								delete
							</v-icon>
						</td>
						<td>{{ props.item.title }}</td>
						<td>{{ props.item.downloadPath }}</td>
						<td>{{ props.item.episode }}</td>
					</template>
				</v-data-table>
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
        return this.$store.state.setting.showSeenList
      },
      set (value) {
        this.$store.commit('setting/setShowSeenList', value)
      }
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        axios.get('/api/setting/seen-list?sort=createDt,desc').then(res => {
          this.items = res.data
        })
      }
    }
  },
  data () {
    return {
      items: [],
      dialog: false,
      windowWidth: 0,
      headers: [
        { text: '동작', value: 'title', sortable: false },
        { text: '제목', value: 'title', sortable: false },
        { text: '다운로드 경로', value: 'downloadPath', sortable: false },
        { text: '에피소드', value: 'episode', sortable: false }
      ]
    }
  },
  mounted () {
    this.windowWidth = window.innerWidth
  },
  methods: {
    close: function () {
      this.editedIndex = -1
      this.regexShow = false
      this.$store.commit('setting/setShowSeenList', false)
    },
    deleteItem: function (item) {
      const index = this.items.indexOf(item)
      if (confirm('이 항목을 삭제하시겠습니까?')) {
        axios.post('/api/setting/seen-list/delete', item).then(res => {
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
        axios.post('/api/setting/seen-list/delete/all').then(res => {
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

