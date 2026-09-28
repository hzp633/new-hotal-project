import request from '../request'

export const adminLogin = (data) => request.post('/admin/auth/login', data)
export const getAdminProfile = () => request.get('/admin/profile')
export const adminLogout = () => request.post('/admin/logout')
