import request from './request'

export const createReservation = (data) => request.post('/reservation', data)
export const payReservation = (id) => request.post(`/reservation/${id}/pay`)
export const getReservations = (params) => request.get('/reservation/list', { params })
export const getReservationDetail = (id) => request.get(`/reservation/${id}`)
export const cancelReservation = (id) => request.post(`/reservation/${id}/cancel`)
export const replicateReservation = (id) => request.get(`/reservation/${id}`)
