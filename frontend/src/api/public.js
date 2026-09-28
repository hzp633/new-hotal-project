import request from './request'

export const getHotel = () => request.get('/public/hotel')
export const getRoomTypes = () => request.get('/public/room-types')
export const searchRoomTypes = (params) => request.get('/public/room-types/search', { params })
export const getRoomTypeDetail = (id) => request.get(`/public/room-types/${id}`)
export const getRoomTypeReviews = (id) => request.get(`/public/room-types/${id}/reviews`)
export const getAnnouncements = () => request.get('/public/announcements')
