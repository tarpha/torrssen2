<template>
	<v-progress-circular
		:rotate="-90"
		:size="50"
		:width="7"
		:value="percentDone"
		color="teal"
	>
		{{ percentDone }}
	</v-progress-circular>
</template>

<script>
import stompClient from '~/plugins/stomp'

export default {
  props: {
    id: {
      type: Number,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    stop: {
      type: Boolean,
      required: true
    }
  },
  watch: {
    stop: function (val) {
      if (val === true) {
        this.count = 0
        this.subscription.unsubscribe()
        clearInterval(this.intervalObj)
        this.$store.commit('download/toggle', {
          active: false,
          stop: false,
          vueIndex: this.index,
          id: 0
        })
      }
    }
  },
  data () {
    return {
      percentDone: 0,
      intervalObj: '',
      subscription: '',
      count: 0
    }
  },
  mounted () {
    if (stompClient.connected() === true) {
      this.subscription = this.subscribe()
    }
    this.intervalObj = setInterval(() => {
      if (stompClient.connected() === false) {
        if (typeof this.subscription.unsubscribe === 'function') {
          this.subscription.unsubscribe()
        }
      }
      if (stompClient.connected() === true) {
        if (this.count < 3) {
          stompClient.publish('/app/rate/' + this.id, '')
          this.count++
        } else {
          this.subscription = this.subscribe()
          this.count = 0
        }
      }
    }, 1000)
  },
  beforeDestroy () {
    this.subscription.unsubscribe()
    clearInterval(this.intervalObj)
  },
  methods: {
    subscribe: function () {
      return stompClient.subscribe('/topic/rate/' + this.id, frame => {
        this.count--
        const body = JSON.parse(frame.body)
        this.percentDone = body.percentDone
        if (body.done === true) {
          clearInterval(this.intervalObj)
          this.$store.commit('snackbar/show', '완료: ' + this.title)
          this.$store.commit('download/toggle', {
            active: false,
            stop: false,
            vueIndex: this.index,
            id: 0
          })
        }
      })
    }
  }
}
</script>

