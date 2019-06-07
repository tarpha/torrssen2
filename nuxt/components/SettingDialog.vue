<template>
	<v-dialog
		v-if="showSetting"
		v-model="showSetting"
		persistent
		max-width="650"
    :fullscreen="windowWidth < 400"
	>
		<v-card>
      <v-toolbar flat extended>
        <v-btn icon @click="close">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title>환경 설정</v-toolbar-title>
        <template v-slot:extension>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            flat="flat"
            @click="save(tabs)"
          >
            저장
          </v-btn>
        </template>
      </v-toolbar>
			<!-- <v-card-title class="headline" v-html="'환경 설정'"></v-card-title> -->
			<v-tabs>
				<v-tab
					v-for="(item, tabIndex) in tabs"
					v-model="currentItem"
					:key="tabIndex"
					:href="'#tab-' + tabIndex"
				>
					{{ item.name }}
				</v-tab>
				<v-tabs-items v-model="currentItem">
					<v-tab-item
						v-for="(item, tabIndex) in tabs"
						:key="tabIndex"
						:value="'tab-' + tabIndex"
					>
						<v-card-text>
							<v-container grid-list-md style="padding-top: 0; padding-bottom: 0">
								<v-layout wrap>
									<v-flex xs12 v-for="(item, index) in item.arr" :key="index">
									<!-- <v-flex xs12 v-for="(item, index) in items" :key="index">
										<v-subheader 
											v-if="index > 0 && items[index -1].groupLabel != item.groupLabel"
											style="padding: 0; color: #1976d2 !important;"
										>{{ item.groupLabel }}</v-subheader> -->
										<v-text-field 
											v-if="item.type == 'text' || item.type == 'number' || item.type == 'password'"
											v-model="item.value" 
											:label="item.label" 
											:type="item.type"
											:required="item.required"
										></v-text-field>
										<v-combobox 
											v-else-if="item.type == 'boolean'"
											v-model="item.value" 
											:label="item.label" 
											:items="['TRUE', 'FALSE']"
											:required="item.required"
										></v-combobox>
										<v-combobox 
											v-else-if="item.type == 'app'"
											v-model="item.value" 
											:label="item.label" 
											:items="['TRANSMISSION', 'DOWNLOAD_STATION']"
											:required="item.required"
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
					@click="save(tabs)"
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
  data () {
    return {
      oris: [],
      tabs: [],
      currentItem: 0,
      windowWidth: 0
    }
  },
  computed: {
    showSetting: {
      get () {
        return this.$store.state.setting.showSetting
      },
      set (value) {
        this.$store.commit('setting/setShowSetting', value)
      }
    }
  },
  watch: {
    showSetting: function (val) {
      if (val === true) {
        this.tabs = []
        axios.get('/api/setting/list?sort=orderId,key').then(res => {
          this.oris = res.data
          let arr = []
          for (let i = 0; i < res.data.length; i++) {
            let obj = res.data[i]
            if (i > 0 && res.data[i - 1].groupLabel !== obj.groupLabel) {
              if (res.data[i - 1].groupLabel !== 'false') {
                this.tabs.push({
                  name: res.data[i - 1].groupLabel,
                  arr: JSON.parse(JSON.stringify(arr))
                })
                arr = []
              }
            }
            arr.push(obj)
          }
          if (arr.length > 0) {
            this.tabs.push({
              name: res.data[res.data.length - 1].groupLabel,
              arr: JSON.parse(JSON.stringify(arr))
            })
          }
        })
      }
    }
  },
  mounted () {
    this.windowWidth = window.innerWidth
  },
  methods: {
    close: function () {
      this.$store.commit('setting/setShowSetting', false)
    },
    save: function (tabs) {
      let items = []
      for (let i = 0; i < tabs.length; i++) {
        for (let j = 0; j < tabs[i].arr.length; j++) {
          for (let k = 0; k < this.oris.length; k++) {
            if (this.oris[k].key === tabs[i].arr[j].key && this.oris[k].value !== tabs[i].arr[j].value) {
              items.push(tabs[i].arr[j])
            } else if (tabs[i].arr[j].key === 'INIT' && tabs[i].arr[j].value === 'FALSE') {
              tabs[i].arr[j].value = 'TRUE'
              items.push(tabs[i].arr[j])
            }
          }
        }
      }
      for (let i = 0; i < items.length; i++) {
        if (items[i].key === 'INIT') {
          items[i].value = 'TRUE'
          break
        }
      }
      axios.post('/api/setting/save', items).then(res => {
        let msg = '저장하였습니다.'
        if (res.status !== 200) {
          msg = '저장하지 못했습니다.'
        }
        this.$store.commit('snackbar/show', msg)
        axios.get('/api/setting/DARK_THEME').then(res => {
          this.$store.commit('setDark', res.data === 'TRUE')
        })
        this.close()
      })
    }
  }
}
</script>
