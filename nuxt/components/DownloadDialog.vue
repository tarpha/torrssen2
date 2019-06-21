<template>
	<v-dialog
		v-model="show"
    v-if="show"
		max-width="550"
	>
		<v-card style="overflow: hidden;">
      <v-toolbar flat extended>
        <v-btn icon @click="close">
          <v-icon>close</v-icon>
        </v-btn>
        <v-toolbar-title v-html="'다운로드 요청' + $store.state.download.text"></v-toolbar-title>
        <template v-slot:extension>
          <v-checkbox 
            v-model="checkWatch"
            label="자동 다운로드" 
            color="primary"
          ></v-checkbox>
        </template>
      </v-toolbar>
			<!-- <v-card-title class="headline" v-html="'다운로드 요청' + $store.state.download.text"></v-card-title> -->

			<div v-if="$store.state.download.result">
        <div :class="successClass">
          <div class="o-circle__sign"></div>  
        </div> 
			</div>
      <div v-else>
			<v-card flat>
       	<v-list two-line>
          <!-- <v-subheader style="text-weight: bold">
            <v-checkbox v-model="checkWatch" label="자동 다운로드" color="primary"></v-checkbox>
          </v-subheader> -->
					<v-list-tile v-for="(item, index) in paths" :key="index"
						@click="if(item.useSeason === false && item.useTitle === false) download(item.path)"
					>
						<v-list-tile-action >
							<v-icon 
                :color="dark !== true ? 'blue-grey darken-2' : 'grey lighten-4'"
                @click="if(item.useSeason === true || item.useTitle === true) download(item.path)"
              >
                get_app
              </v-icon>
						</v-list-tile-action>
						<v-list-tile-content v-if="!item.useSeason && !item.useTitle">
							<v-list-tile-title>{{ item.name }}</v-list-tile-title>
							<v-list-tile-sub-title>{{ item.path }}</v-list-tile-sub-title>
						</v-list-tile-content>
            <v-list-tile-content v-else>
							<v-text-field 
                v-model="item.path"
                :label="item.name"
                style="width: 90%"
                @keyup.enter="download(item.path)"
              ></v-text-field>
						</v-list-tile-content>
					</v-list-tile>
          <v-list-tile>
						<v-list-tile-action>
							<v-icon 
                @click="download(customPath)" 
                :color="dark !== true ? 'blue-grey darken-2' : 'grey lighten-4'"
              >
                get_app
              </v-icon>
						</v-list-tile-action>
						<v-list-tile-content>
							<v-text-field 
                v-model="customPath" 
                label="사용자 지정 경로"
                style="width: 90%"
              ></v-text-field>
						</v-list-tile-content>
					</v-list-tile>
				</v-list>
			</v-card>
      </div>
			<!-- <v-card-actions>
				<v-spacer></v-spacer>

				<v-btn
          v-if="$store.state.download.result === false"
					color="primary"
					flat="flat"
					@click="close"
				>
					닫기
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
      successClass: 'o-circle c-container__circle o-circle__sign--success',
      seasonPath: '',
      customPath: '',
      paths: []
    }
  },
  computed: {
    show: {
      get () {
        return this.$store.state.download.show
      },
      set (value) {
        this.$store.commit('download/setShow', value)
      }
    },
    checkWatch: {
      get () {
        return this.$store.state.download.auto
      },
      set (value) {
        this.$store.commit('download/setAuto', value)
      }
    },
    pathList () {
      return this.$store.state.download.path
    },
    data () {
      return this.$store.state.download.data
    },
    dark: function () {
      return this.$store.state.dark
    }
  },
  watch: {
    show: function (val) {
      if (val === false) {
        this.seasonPath = ''
        this.customPath = ''
      } else {
        this.paths = JSON.parse(JSON.stringify(this.pathList))
      }
    }
  },
  methods: {
    download: function (path) {
      if (!path) {
        this.$store.commit('snackbar/show', '경로를 입력해주세요.')
        return
      }
      axios.post('/api/download/create', {
        'name': this.$store.state.download.data.title,
        'uri': this.$store.state.download.data.link,
        'rssTitle': this.$store.state.download.data.rssTitle,
        'rssReleaseGroup': this.$store.state.download.data.rssReleaseGroup,
        'vueItemIndex': this.$store.state.download.index,
        'downloadPath': path,
        'auto': this.$store.state.download.auto
      }).then(ret => {
        if (ret.data > 0) {
          this.successClass = 'o-circle c-container__circle o-circle__sign--success'
          this.$store.commit('download/setText', '&nbsp<span style=\'color: green\'>성공</span>')

          this.$store.commit('download/toggle', {
            active: true,
            stop: false,
            vueIndex: this.$store.state.download.index,
            id: ret.data
          })
        } else {
          if (ret.data === -2) {
            this.$store.commit('download/setText', '&nbsp<span style=\'color: red\'>중복</span>')
          } else {
            this.$store.commit('download/setText', '&nbsp<span style=\'color: red\'>실패</span>')
          }
          this.successClass = 'o-circle c-container__circle o-circle__sign--failure'
        }
        this.$store.commit('download/setResult', true)

        setTimeout(() => {
          this.close()
        }, 2000)
      })
    },
    close: function () {
      this.$store.commit('download/setShow', false)
    }
  }
}
</script>

<style>
HTML {
  /*using system font-stack*/
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen-Sans, Ubuntu, Cantarell, "Helvetica Neue", sans-serif;
  font-size: 115%; /*~18px*/
  font-size: calc(12px + (25 - 12) * (100vw - 300px) / (1300 - 300) );
  line-height: 1.5;
  box-sizing: border-box;
}

BODY {
  margin: 0;
  color: #3a3d40;
  background: rgb(252, 252, 252);
}

*, *::before, *::after {
  box-sizing: inherit;
  color: inherit;
}

/*Actual Style*/
 
/*=======================
       C-Container
=========================*/
.c-container {
  max-width: 27rem;
  margin: 1rem auto 0;
  padding: 1rem;
}

/*=======================
       O-Circle
=========================*/

.o-circle {
  display: flex;
  width: 10.555rem; height: 10.555rem;
  justify-content: center;
  align-items: flex-start;
  border-radius: 50%; 
  animation: circle-appearance .8s ease-in-out 1 forwards, set-overflow .1s 1.1s forwards;
}

.c-container__circle {
  margin: 20px auto;
}

/*=======================
    C-Circle Sign
=========================*/
      
.o-circle__sign {
  position: relative;
  opacity: 0;
  background: #fff;
  animation-duration: .8s;
  animation-delay: .2s;
  animation-timing-function: ease-in-out;
  animation-iteration-count: 1;
  animation-fill-mode: forwards;
}

.o-circle__sign::before, 
.o-circle__sign::after {
  content: "";
  position: absolute;
  background: inherit;
}

.o-circle__sign::after {
  left: 100%; top: 0%;
  width: 500%; height: 95%; 
  transform: translateY(4%) rotate(0deg);
  border-radius: 0;
  opacity: 0;
  animation: set-shaddow 0s 1.13s ease-in-out forwards;
  z-index: -1;
}


/*=======================
      Sign Success
=========================*/
 
.o-circle__sign--success { 
  background: rgb(56, 176, 131);
}

.o-circle__sign--success .o-circle__sign {
  width: 1rem; height: 6rem;
  border-radius: 50% 50% 50% 0% / 10%;
  transform: translateX(130%) translateY(35%) rotate(45deg) scale(.11);  
  animation-name: success-sign-appearance;
}

.o-circle__sign--success .o-circle__sign::before {
   bottom: -17%;
   width: 100%; height: 50%; 
   transform: translateX(-130%) rotate(90deg);
   border-radius: 50% 50% 50% 50% / 20%;

}

/*--shadow--*/
.o-circle__sign--success .o-circle__sign::after {
   background: rgb(40, 128, 96);
}
 

/*=======================
      Sign Failure
=========================*/

.o-circle__sign--failure {
  background: rgb(236, 78, 75);
}

.o-circle__sign--failure .o-circle__sign {
  width: 1rem; height: 7rem;
  transform: translateY(25%) rotate(45deg) scale(.1);
  border-radius: 50% 50% 50% 50% / 10%;
  animation-name: failure-sign-appearance;
}

.o-circle__sign--failure .o-circle__sign::before {
   top: 50%;
   width: 100%; height: 100%; 
   transform: translateY(-50%) rotate(90deg);
   border-radius: inherit;
} 

/*--shadow--*/
.o-circle__sign--failure .o-circle__sign::after {
   background: rgba(175, 57, 55, .8);
}


/*-----------------------
      @Keyframes
--------------------------*/
 
/*CIRCLE*/
@keyframes circle-appearance {
  0% { transform: scale(0); }
  
  50% { transform: scale(1.5); }
          
  60% { transform: scale(1); }
  
  100% { transform: scale(1); }
}

/*SIGN*/
@keyframes failure-sign-appearance {         
  50% { opacity: 1;  transform: translateY(25%) rotate(45deg) scale(1.7); }
    
  100% { opacity: 1; transform: translateY(25%) rotate(45deg) scale(1); }
}

@keyframes success-sign-appearance {      
  50% { opacity: 1;  transform: translateX(130%) translateY(35%) rotate(45deg) scale(1.7); }
    
  100% { opacity: 1; transform: translateX(130%) translateY(35%) rotate(45deg) scale(1); }
}
 

@keyframes set-shaddow {
  to { opacity: 0; }
}

@keyframes set-overflow {
  to { overflow: hidden; }
}


/*+++++++++++++++++++
    @Media Queries
+++++++++++++++++++++*/

@media screen and (min-width: 1300px) {
  
  HTML { font-size: 1.5625em; } /* 25 / 16 = 1.5625 */
  
}
</style>
