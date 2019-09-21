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
        <v-toolbar-title>자동 다운로드 관리</v-toolbar-title>
        <template v-slot:extension>
          <v-text-field
            v-model="searchTitle"
            label="검색"
            clearable
            @keyup.enter="search"
            @click:clear="clear"
          ></v-text-field>
          <v-spacer></v-spacer>
          <v-btn
						color="primary"
						flat="flat"
						@click="changeAll"
					>
						일괄 변경
					</v-btn>
					<v-btn
						color="primary"
						flat="flat"
						@click="execute"
					>
						지금 실행
					</v-btn>
          <v-btn
						color="primary"
						flat="flat"
						@click="add"
					>
						추가
					</v-btn>
        </template>
      </v-toolbar>
				<v-data-table
					v-model="tableSelected"
					:headers="headers"
					:items="items"
					rows-per-page-text=""
					item-key="title"
					select-all
				>
					<template v-slot:items="props">
						<td>
							<v-checkbox
								v-model="props.selected"
								:input-value="props.selected"
								primary
								hide-details
							></v-checkbox>
						</td>
						<td>
							<v-icon
								small
								class="mr-2"
								@click="editItem(props.item)"
							>
								edit
							</v-icon>
							<v-icon
								small
								@click="deleteItem(props.item)"
							>
								delete
							</v-icon>
						</td>
						<td>{{ props.item.title }}</td>
						<td>{{ props.item.use }}</td>
						<td>{{ props.item.downloadPath }}</td>
					</template>
				</v-data-table>
			<v-card-actions>
				<v-spacer></v-spacer>
			</v-card-actions>
		</v-card>
	</v-dialog>
	<v-dialog 
		v-model="dialog"
    persistent
		max-width="650px"
		:fullscreen="windowWidth < 400"
	>
		<v-card>
			<v-toolbar flat extended>
        <v-btn icon @click="dialog = false">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title>{{ formTitle }}</v-toolbar-title>
        <template v-slot:extension>
          <v-spacer></v-spacer>
          <v-btn
						color="primary"
						flat="flat"
						@click="save"
					>
						저장
					</v-btn>
        </template>
      </v-toolbar>
			<v-card-text>
				<v-container grid-list-md>
					<v-layout wrap>
						<v-flex xs12 >
							<v-text-field
								ref="title"
								v-model="editedItem.title"
								label="포함될 단어" 
								:readonly="editedIndex >= 0"
							></v-text-field>
						</v-flex>
						<v-flex xs6>
							<v-combobox
								v-model="editedItem.use" 
								label="사용여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs5>
							<v-combobox
								v-model="editedItem.useRegex" 
								label="정규식 사용" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex
              xs1 
              style="margin-top: 0.3rem"
            >
							<v-tooltip bottom>
								<template v-slot:activator="{ on }">
									<v-icon v-on="on" @click="testRegex">check</v-icon>
								</template>
								<span>일치 항목 검사</span>
							</v-tooltip>
						</v-flex>
						<v-data-table
							v-if="regexShow"
							hide-headers
							hide-actions
    						:items="regexItems"
							style="width: 100%"
  						>
							<template v-slot:items="props">
								<td>{{ props.item.title }}</td>
							</template>
						</v-data-table>
						<v-flex xs6>
							<v-text-field
								v-model="editedItem.quality" 
								label="화질"
								hint="+로 상위 화질 검색 ex) 720p+"
							></v-text-field>
						</v-flex>
						<v-flex xs6>
							<v-text-field v-model="editedItem.releaseGroup" label="릴 그룹"></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-combobox
								v-model.lazy="editedItem.downloadPath" 
								label="다운로드 경로" 
								:items="pathList"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs6>
							<v-combobox
								v-model="editedItem.subtitle" 
								label="자막여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs6>
							<v-combobox
								v-model="editedItem.series" 
								label="시리즈여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs6>
							<v-text-field v-model="editedItem.startSeason" label="다운로드 시작 시즌"></v-text-field>
						</v-flex>
						<v-flex xs6>
							<v-text-field v-model="editedItem.startEpisode" label="다운로드 시작 에피소드"></v-text-field>
						</v-flex>
						<v-flex xs6>
							<v-text-field v-model="editedItem.endSeason" label="다운로드 종료 시즌"></v-text-field>
						</v-flex>
						<v-flex xs6>
							<v-text-field v-model="editedItem.endEpisode" label="다운로드 종료 에피소드"></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
								v-model="editedItem.rename"
								label="변경할 파일명"
								hint="${TITLE}, ${SEASON}, ${EPISODE}, ${QUALITY}, ${RELEASE_GROUP}, ${DATE} 변수 사용 가능"
							></v-text-field>
						</v-flex>
					</v-layout>
				</v-container>
			</v-card-text>
		</v-card>
	</v-dialog>
  <v-dialog 
		v-model="changeAllMode"
    persistent
		max-width="650px"
		:fullscreen="windowWidth < 400"
	>
		<v-card>
			<v-toolbar flat extended>
        <v-btn icon @click="changeAllMode = false">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title>일괄 변경</v-toolbar-title>
        <template v-slot:extension>
          <v-spacer></v-spacer>
          <v-btn
						color="primary"
						flat="flat"
						@click="saveAll"
					>
						저장
					</v-btn>
        </template>
      </v-toolbar>
			<v-card-text>
				<v-container grid-list-md>
					<v-layout wrap>
            <v-flex xs12>
							<v-combobox
                v-model="selected" 
								label="항목 선택" 
								:items="editedAllItemList"
								multiple
                chips
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-combobox
                v-if="selected.find(obj => obj.value === 'use')"
								v-model="editedAllItem.use" 
								label="사용여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-combobox
                v-if="selected.find(obj => obj.value === 'useRegex')"
								v-model="editedAllItem.useRegex" 
								label="정규식 사용" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'quality')"
								v-model="editedAllItem.quality" 
								label="화질"
								hint="+로 상위 화질 검색 ex) 720p+"
							></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'releaseGroup')"
                v-model="editedAllItem.releaseGroup"
                label="릴 그룹"
              ></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-combobox
                v-if="selected.find(obj => obj.value === 'downloadPath')"
								v-model.lazy="editedAllItem.downloadPath" 
								label="다운로드 경로" 
								:items="pathList"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-combobox
                v-if="selected.find(obj => obj.value === 'subtitle')"
								v-model="editedAllItem.subtitle" 
								label="자막여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-combobox
                v-if="selected.find(obj => obj.value === 'series')"
								v-model="editedAllItem.series" 
								label="시리즈여부" 
								:items="[true, false]"
								required
							></v-combobox>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'startSeason')"
                v-model="editedAllItem.startSeason"
                label="다운로드 시작 시즌"
              ></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'startEpisode')"
                v-model="editedAllItem.startEpisode"
                label="다운로드 시작 에피소드"
              ></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'endSeason')"
                v-model="editedAllItem.endSeason"
                label="다운로드 종료 시즌"
              ></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'endEpisode')"
                v-model="editedAllItem.endEpisode"
                label="다운로드 종료 에피소드"
              ></v-text-field>
						</v-flex>
						<v-flex xs12>
							<v-text-field
                v-if="selected.find(obj => obj.value === 'rename')"
								v-model="editedAllItem.rename"
								label="변경할 파일명"
								hint="${TITLE}, ${SEASON}, ${EPISODE}, ${QUALITY}, ${RELEASE_GROUP}, ${DATE} 변수 사용 가능"
							></v-text-field>
						</v-flex>
					</v-layout>
				</v-container>
			</v-card-text>
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
        return this.$store.state.setting.showWatchList
      },
      set (value) {
        this.$store.commit('setting/setShowWatchList', value)
      }
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        this.searchTitle = ''
        this.tableSelected = []
        axios.get('/api/setting/watch-list?sort=createDt,desc').then(res => {
          this.items = res.data
        })
        axios.get('/api/setting/path').then(res => {
          this.pathList = []
          for (let i = 0; i < res.data.length; i++) {
            this.pathList.push(res.data[i].name)
          }
        })
      }
    }
  },
  data () {
    return {
      items: [],
      pathList: [],
      selected: [],
      tableSelected: [],
      dialog: false,
      formTitle: '',
      editedItem: {},
      editedAllItem: {},
      editedIndex: -1,
      regexItems: [],
      regexShow: false,
      windowWidth: 0,
      changeAllMode: false,
      searchTitle: '',
      defaultItem: {
        title: '',
        use: true,
        useRegex: false,
        downloadPath: '',
        quality: '720p+',
        releaseGroup: '',
        startSeason: '01',
        startEpisode: '01',
        endSeason: '99',
        endEpisode: '999',
        subtitle: false,
        series: true,
        rename: '',
        createDt: new Date()
      },
      editedAllItemList: [
        { text: '사용여부', value: 'use' },
        { text: '정규식 사용', value: 'useRegex' },
        { text: '화질', value: 'quality' },
        { text: '릴 그룹', value: 'releaseGroup' },
        { text: '다운로드 경로', value: 'downloadPath' },
        { text: '자막여부', value: 'subtitle' },
        { text: '시리즈여부', value: 'series' },
        { text: '다운로드 시작 시즌', value: 'startSeason' },
        { text: '다운로드 종료 시즌', value: 'endSeason' },
        { text: '다운로드 시작 에피소드', value: 'startEpisode' },
        { text: '다운로드 종료 에피소드', value: 'endEpisode' },
        { text: '변경할 파일명', value: 'rename' }
      ],
      headers: [
        { text: '동작', value: 'title', sortable: false },
        { text: '포함할 단어', value: 'title', sortable: false },
        { text: '사용여부', value: 'use', sortable: false },
        { text: '다운로드 경로', value: 'downloadPath', sortable: false }
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
      this.$store.commit('setting/setShowWatchList', false)
    },
    add: function () {
      this.changeAllMode = false
      this.dialog = true
      this.formTitle = '자동 다운로드 추가'
      this.editedIndex = -1
      this.editedItem = Object.assign({}, this.defaultItem)
    },
    execute: function () {
      if (this.tableSelected.length === 0) {
        alert('실행할 항목을 선택해 주세요.')
        return
      }
      axios.post('/api/setting/watch-list/execute', this.tableSelected).then(res => {
        let msg = '실행 요청하였습니다.'
        if (res.status !== 200) {
          msg = '실행 요청에 실패하였습니다.'
        }
        this.$store.commit('snackbar/show', msg)
      })
    },
    editItem: function (item) {
      this.changeAllMode = false
      this.editedIndex = this.items.indexOf(item)
      this.editedItem = Object.assign({}, item)
      this.formTitle = '자동 다운로드 편집'
      this.dialog = true
    },
    deleteItem: function (item) {
      const index = this.items.indexOf(item)
      if (confirm('이 항목을 삭제하시겠습니까?')) {
        axios.post('/api/setting/watch-list/delete', item).then(res => {
          let msg = '삭제하였습니다.'
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          this.$delete(this.items, index)
          this.$store.commit('snackbar/show', msg)
        })
      }
    },
    save: function () {
      this.$refs.title.focus()
      setTimeout(() =>
        axios.post('/api/setting/watch-list', this.editedItem).then(res => {
          let msg = '저장하였습니다.'
          if (res.status !== 200) {
            msg = '저장하지 못했습니다.'
          }
          this.$store.commit('snackbar/show', msg)
          // this.items.push(this.editedItem)
          axios.get('/api/setting/watch-list').then(res => {
            this.items = res.data
          })
          this.dialog = false
        }), 100)
    },
    saveAll: function () {
      if (confirm('일괄 변경하시겠습니까?')) {
        axios.post('/api/setting/watch-list/all', {
          watchList: this.editedAllItem,
          selectList: this.selected
        }).then(res => {
          let msg = '저장하였습니다.'
          if (res.status !== 200) {
            msg = '저장하지 못했습니다.'
          }
          this.$store.commit('snackbar/show', msg)
          axios.get('/api/setting/watch-list').then(res => {
            this.items = res.data
          })
          this.changeAllMode = false
        })
      }
    },
    testRegex: async function () {
      const res = await axios.get('/api/rss/feed/regex/test', {
        params: {
          title: this.editedItem.title
        }
      })
      this.regexShow = true
      this.regexItems = res.data
      setTimeout(() => {
        this.regexShow = false
        this.regexItems = []
      }, 6000)
    },
    changeAll: function () {
      this.selected = []
      this.editedAllItem = Object.assign({}, this.defaultItem)
      this.changeAllMode = true
    },
    search: function () {
      axios.get('/api/setting/watch-list/search', {
        params: {
          title: this.searchTitle,
          sort: 'createDt,desc'
        }
      }).then(res => {
        this.items = res.data
      })
    },
    clear: function () {
      axios.get('/api/setting/watch-list?sort=createDt,desc').then(res => {
        this.items = res.data
      })
    }
  }
}
</script>

