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
        <v-toolbar-title>RSS 사이트 관리</v-toolbar-title>
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
			<!-- <v-card-title class="headline" v-html="'RSS 사이트 관리'"></v-card-title> -->
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
											v-model="item.url" 
											label="주소"
											required
										></v-text-field>
									</v-flex>
									<v-flex xs12 sm6>
										<v-combobox
											v-model="item.useDb" 
											label="사용여부" 
											:items="[true, false]"
											required
										></v-combobox>
									</v-flex>
									<v-flex xs12 sm6>
										<v-combobox
											v-model="item.tvSeries" 
											label="TV시리즈 여부(제목 파싱)" 
											:items="[true, false]"
											required
										></v-combobox>
									</v-flex>
									<!-- <v-flex xs12 sm6>
										<v-text-field
											v-model="item.linkKey" 
											label="마그넷 링크가 포함된 필드 키 (베타)"
										></v-text-field>
									</v-flex> -->
								</v-layout>
							</v-container>
						</v-card-text>
					</v-tab-item>
				</v-tabs-items>
			</v-tabs>
			<v-card-actions>
				<v-spacer></v-spacer>
				<v-btn
					color="primary"
					flat="flat"
					@click="reload"
				>
					RSS 갱신
				</v-btn>
			</v-card-actions>
		</v-card>
	</v-dialog>
</template>

<script>
import axios from '~/plugins/axios'

export default {
  data () {
    return {
      currentItem: 0,
      windowWidth: 0,
      items: []
    }
  },
  computed: {
    show: {
      get () {
        return this.$store.state.setting.showRssList
      },
      set (value) {
        this.$store.commit('setting/setShowRssList', value)
      }
    }
  },
  watch: {
    show: function (val) {
      if (val === true) {
        axios.get('/api/setting/rss-list').then(res => {
          this.items = res.data
        })
      }
    }
  },
  mounted () {
    this.windowWidth = window.innerWidth
  },
  methods: {
    close: function () {
      this.$store.commit('setting/setShowRssList', false)
    },
    add: function () {
      this.items.push({
        name: 'RSS-' + this.items.length,
        url: '',
        useDb: true,
        tvSeries: true,
        linkKey: 'link',
        createDt: new Date()
      })
      this.currentItem = 'tab-' + (this.items.length - 1)
    },
    deleteTab: function (index) {
      const itemIndex = parseInt(index.replace('tab-', ''))
      const delItem = JSON.parse(JSON.stringify(this.items[itemIndex]))
      if (confirm('삭제하시겠습니까?')) {
        this.$delete(this.items, itemIndex)
        axios.post('/api/setting/rss-list', this.items).then(res => {
          let msg = '삭제하였습니다..'
          if (res.status !== 200) {
            msg = '삭제하지 못했습니다.'
          }
          if (confirm('이 RSS의 데이터를 삭제하시겠습니까?')) {
            axios.post('/api/rss/feed/delete/rss-site', delItem).then(res => {
              let msg = '삭제하였습니다.'
              if (res.status !== 200) {
                msg = '삭제하지 못했습니다.'
              }
              this.$store.commit('snackbar/show', msg)
              this.$store.commit('toolbar/toggle')
            })
          } else {
            this.$store.commit('snackbar/show', msg)
          }
        })
      }
    },
    save: function () {
      axios.post('/api/setting/rss-list', this.items).then(res => {
        let msg = '저장하였습니다.'
        if (res.status !== 200) {
          msg = '저장하지 못했습니다.'
        }
        this.$store.commit('snackbar/show', msg)
        axios.post('/api/rss/reload', {})
        this.close()
      })
    },
    reload: function () {
      axios.post('/api/rss/reload', {}).then(res => {
        let msg = '갱신 요청하였습니다.'
        if (res.status !== 200) {
          msg = '갱신 요청에 실패하였습니다.'
        }
        this.$store.commit('snackbar/show', msg)
      })
    }
  }
}
</script>
