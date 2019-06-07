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
      subscription: ''
    }
  },
  mounted () {
    this.subscription = this.subscribe()
    this.intervalObj = setInterval(() => {
      stompClient.publish('/app/rate/' + this.id, '')
      this.count++
    }, 1000)
  },
  methods: {
    subscribe: function () {
      return stompClient.subscribe('/topic/rate/' + this.id, frame => {
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

