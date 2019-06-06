import axios from 'axios'

export default axios.create({
  // baseURL: process.env.baseUrl
  baseURL: process.env.BASE_URL
})
