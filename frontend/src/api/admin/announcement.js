import request from '../request'

export const getAnnouncementPage = (params) => request.get('/admin/announcement/page', { params })
export const createAnnouncement = (data) => request.post('/admin/announcement', data)
export const updateAnnouncement = (data) => request.put('/admin/announcement', data)
export const updateAnnouncementStatus = (id, status) => request.put(`/admin/announcement/${id}/status`, null, { params: { status } })
export const deleteAnnouncement = (id) => request.delete(`/admin/announcement/${id}`)
