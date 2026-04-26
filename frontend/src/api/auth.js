import request from '@/utils/request'

export const authApi = {
  // 登录
  login: (data) => request.post('/auth/login', data),
  
  // 注册
  register: (data) => request.post('/auth/register', data),
  
  // 发送注册验证码
  sendCode: (email) => request.post('/auth/code', null, { params: { email } }),
  
  // 发送重置密码验证码
  sendResetCode: (email) => request.post('/auth/code/reset', null, { params: { email } }),
  
  // 重置密码
  resetPassword: (data) => request.post('/auth/reset-password', data),
  
  // 修改密码
  changePassword: (data) => request.post('/auth/change-password', data),
  
  // 注销账户
  deactivateAccount: (data) => request.post('/auth/deactivate', data),
  
  // 检查用户名是否可用
  checkUsername: (username) => request.get('/auth/check-username', { params: { username } }),
  
  // 检查邮箱是否可用
  checkEmail: (email) => request.get('/auth/check-email', { params: { email } })
}