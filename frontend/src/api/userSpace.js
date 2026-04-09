import request from '@/utils/request'

export const userSpaceApi = {
  getMyProfile: () => request.get('/profile/info'),
  getUserProfile: (userId) => request.get(`/user/${userId}/profile`),
  listMyFolders: (keyword) => request.get('/collection/folders', { params: { keyword: (keyword || '').trim() || undefined } }),
  listUserPublicFolders: (userId) => request.get(`/user/${userId}/collection/folders`)
}

export const followApi = {
  follow: (userId) => request.post(`/follow/${userId}`),
  unfollow: (userId) => request.delete(`/follow/${userId}`)
}

export const privacyApi = {
  getMySettings: () => request.get('/privacy/settings'),
  updateMySettings: (data) => request.put('/privacy/settings', data),
  getUserVisibility: (userId) => request.get(`/user/${userId}/privacy`)
}

export const dynamicsApi = {
  listUserDynamics: (userId, params) => request.get(`/user/${userId}/dynamics`, { params })
}

export const guestbookApi = {
  listReceived: (userId, params) => request.get(`/guestbook/received/${userId}`, { params }),
  listSent: (params) => request.get('/guestbook/sent', { params }),
  add: (data) => request.post('/guestbook', data),
  reply: (id, data) => request.put(`/guestbook/reply/${id}`, data),
  remove: (id) => request.delete(`/guestbook/${id}`),
  removeReply: (id) => request.delete(`/guestbook/reply/${id}`)
}
