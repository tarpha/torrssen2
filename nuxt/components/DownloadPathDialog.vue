<template>
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
        <v-toolbar-title>다운로드 경로 관리</v-toolbar-title>
        <template v-slot:extension>
          <v-spacer></v-spacer>
          <v-btn
						color="primary"
						flat="flat"
						@click="deleteTab(currentItem)"
					>
						삭제
					</v-btn>
					<v-btn
						color="primary"
						flat="flat"
						@click="add"
					>
						추가
					</v-btn>
					<v-btn
						color="primary"
						flat="flat"
						@click="save"
					>
						저장
					</v-btn>
        </template>
      </v-toolbar>
			<!-- <v-card-title class="headline" v-html="'다운로드 경로 관리'"></v-card-title> -->
			<v-tabs>
				<v-tab
					v-for="(item, index) in items"
					v-model="currentItem"
					:key="index"
					:href="'#tab-' + index"
				>
					{{ item.name }}
				</v-tab>
				<v-tabs-items v-model="currentItem">
					<v-tab-item
						v-for="(item, index) in items"
						:key="index"
						:value="'tab-' + index"
					>
					<v-card-text>
							<v-container grid-list-md style="padding-top: 0; padding-bottom: 0">
								<v-layout wrap>
									<v-flex xs12>
										<v-text-field
											v-model="item.name" 
											label="이름"
											required
										></v-text-field>
									</v-flex>
									<v-flex xs12>
										<v-text-field
											v-model="item.path" 
											label="경로"
											required
										></v-text-field>
									</v-flex>
									<v-flex xs12 sm6>
										<v-combobox
											v-model="item.useTitle" 
											label="경로에 타이틀 추가" 
											:items="[true, false]"
											required
										></v-combobox>
									</v-flex>
									<v-flex xs12 sm6>
										<v-combobox
											v-model="item.useSeason" 
											label="경로에 시즌 추가" 
											:items="[true, false]"
											required
										></v-combobox>
									</v-flex>
								</v-layout>
							</v-container>
						</v-card-text>
					</v-tab-item>
				</v-tabs-items>
			</v-tabs>
			<!-- <v-card-actions>
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
					@click="deleteTab(currentItem)"
				>
					삭제
				</v-btn>
				<v-btn
					color="primary"
					flat="flat"
					@click="add"
				>
					추가
				</v-btn>
				<v-btn
					color="primary"
					flat="flat"
					@click="save"
				>
					저장
				</v-btn>
			</v-card-actions> -->
		</v-card>
	</v-dialog>
</template>

<script>
import axios from '~/plugins/axios'

export default {
  computed: {
    show: {
      get () {
        return this.$store.state.setting.showDownloadPath
      },
      set (value) {
        this.$store.commit('setting/setShowDownloadPath', value)
      }
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        axios.get('/api/setting/path').then(res => {
          this.items = res.data
        })
      }
    }
  },
  data () {
    return {
      items: [],
      currentItem: 0,
      windowWidth: 0
    }
  },
  mounted () {
    this.windowWidth = window.innerWidth
  },
  methods: {
    close: function () {
      this.$store.commit('setting/setShowDownloadPath', false)
    },
    add: function () {
      this.items.push({
        name: 'PATH-' + this.items.length,
        path: '',
        useTitle: false,
        useSeason: false,
        createDt: new Date()
      })
      this.currentItem = 'tab-' + (this.items.length - 1)
    },
    deleteTab: function (index) {
      if (confirm('삭제하시겠습니까?')) {
        this.$delete(this.items, parseInt(index.replace('tab-', '')))
        axios.post('/api/setting/path', this.items).then(res => {
          let msg = '삭제하였습니다.'
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          this.$store.commit('snackbar/show', msg)
        })
      }
    },
    save: function () {
      axios.post('/api/setting/path', this.items).then(res => {
        let msg = '저장하였습니다.'
        if (res.status !== 200) {
          msg = '저장하지 못했습니다.'
        }
        this.$store.commit('snackbar/show', msg)
        this.close()
      })
    }
  }
}
</script>
