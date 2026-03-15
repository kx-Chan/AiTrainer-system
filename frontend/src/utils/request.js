import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:3000/api', // 后端 API 地址
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('jwt_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 获取后端返回的统一响应对象 Result<T>
    const res = response.data
    
    // 如果 code 不是 200，视为业务错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    
    // 返回 Result<T> 中的 data 字段
    return res.data
  },
  error => {
    // 对响应错误做点什么 (如 401, 403, 500 等 HTTP 状态码错误)
    console.error('API Error:', error)
    
    // 优先尝试获取后端 Result.error 中的消息
    const message = error.response?.data?.message || '网络错误，请稍后再试'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
