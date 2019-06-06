import axios from 'axios'

let options = {}

if (process.env.NODE_ENV !== 'production') {
  options = { baseURL: process.env.baseUrl }
}

export default axios.create(options)
