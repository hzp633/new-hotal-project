import request from '../request'

export const getPendingCheckIns = () => request.get('/admin/checkin/pending')
export const checkIn = (data) => request.post('/admin/checkin', data)
export const getActiveCheckIns = () => request.get('/admin/checkin/active')
export const checkOut = (reservationId, data) => request.post(`/admin/checkin/${reservationId}/checkout`, data)
export const getCheckInPage = (params) => request.get('/admin/checkin/page', { params })
