<template>
  <v-app :dark="dark">
    <v-content>
      <nuxt-toolbar />
      <nuxt />
    </v-content>
  </v-app>
</template>

<script>
  import NuxtToolbar from '~/components/Toolbar'

  export default {
    components: {
      NuxtToolbar
    },
    data () {
      return {
      }
    },
    computed: {
      dark: function () {
        return this.$store.state.dark
      }
    }
  }
</script>

<style>
.v-input--selection-controls {
  padding-top: 10px !important;
}
</style>
