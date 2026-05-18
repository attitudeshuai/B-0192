import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})

service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

export default service
