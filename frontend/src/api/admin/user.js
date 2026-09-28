import request from '../request'

export const getUserPage = (params) => request.get('/admin/user/page', { params })
export const getUserDetail = (id) => request.get(`/admin/user/${id}`)
export const updateUserStatus = (id, status) => request.put(`/admin/user/${id}/status`, null, { params: { status } })
