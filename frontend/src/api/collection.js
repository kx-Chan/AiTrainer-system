import request from '@/utils/request'

export const folderApi = {
  // 获取收藏夹列表（带关键字搜索）
  list: (keyword) => request.get('/collection/folders', { params: { keyword: (keyword || '').trim() || undefined } }),
  
  // 获取收藏夹详情
  get: (id) => request.get(`/collection/folders/${id}`),
  
  // 创建收藏夹
  create: (data) => request.post('/collection/folders', data),
  
  // 更新收藏夹信息
  update: (id, data) => request.put(`/collection/folders/${id}`, data),
  
  // 设为默认（排他性更新）
  setDefault: (id) => request.patch(`/collection/folders/${id}/default`),
  
  // 删除收藏夹
  remove: (id) => request.delete(`/collection/folders/${id}`)
}

export const itemApi = {
  // 检查是否已收藏
  checkFavorited: (postId) => request.get('/collection/favorited', { params: { postId } }),
  
  // 获取已收藏的文件夹ID列表
  getInFolderIds: (postId) => request.get('/collection/items', { params: { postId } }),
  
  // 添加到某个文件夹
  add: (postId, folderId) => request.post('/collection/item', { postId, folderId }),
  
  // 从某个文件夹移除
  remove: (postId, folderId) => request.delete('/collection/item', { data: { postId, folderId } }),
  
  // 获取收藏夹内的推文列表（分页）
  getPostsInFolder: (folderId, params) => request.get(`/collection/folders/${folderId}/posts`, { params })
}
