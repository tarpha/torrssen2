<template>
  <v-app>
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
    }
  }
</script>
