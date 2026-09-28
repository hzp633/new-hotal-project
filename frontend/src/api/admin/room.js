import request from '../request'

export const getRoomPage = (params) => request.get('/admin/room/page', { params })
export const createRoom = (data) => request.post('/admin/room', data)
export const updateRoom = (data) => request.put('/admin/room', data)
export const updateRoomStatus = (id, status) => request.put(`/admin/room/${id}/status`, null, { params: { status } })
export const deleteRoom = (id) => request.delete(`/admin/room/${id}`)
