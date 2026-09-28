import request from '../request'

export const getRoomTypePage = (params) => request.get('/admin/room-type/page', { params })
export const createRoomType = (data) => request.post('/admin/room-type', data)
export const updateRoomType = (data) => request.put('/admin/room-type', data)
export const updateRoomTypeStatus = (id, status) => request.put(`/admin/room-type/${id}/status`, null, { params: { status } })
export const deleteRoomType = (id) => request.delete(`/admin/room-type/${id}`)
