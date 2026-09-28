import request from '../request'

export const getReservationPage = (params) => request.get('/admin/reservation/page', { params })
export const getReservationDetail = (id) => request.get(`/admin/reservation/${id}`)
export const cancelReservation = (id) => request.post(`/admin/reservation/${id}/cancel`)
