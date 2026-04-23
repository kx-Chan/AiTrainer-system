import axios from "axios";
import { ElMessage } from "element-plus";

// 创建 axios 实例
const request = axios.create({
  baseURL: "http://localhost:3000/api", // 确保这里指向你的 Spring Boot 地址
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 1. 获取 Token
    const token = localStorage.getItem("jwt_token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    // ✅ 核心修改：删除了 config.adapter 块
    // 现在请求会通过真正的网络发出去

    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器 (逻辑保持不变)
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || "操作失败");
      return Promise.reject(new Error(res.message || "操作失败"));
    }
    return res.data;
  },
  (error) => {
    console.error("API Error:", error);
    const message = error.response?.data?.message || "网络错误";
    ElMessage.error(message);
    return Promise.reject(error);
  },
);

export default request;
