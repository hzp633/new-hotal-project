import request from './request'

export const getGuests = () => request.get('/guest/list')
export const addGuest = (data) => request.post('/guest', data)
export const updateGuest = (data) => request.put('/guest', data)
export const deleteGuest = (id) => request.delete(`/guest/${id}`)
export const setDefaultGuest = (id) => request.put(`/guest/${id}/default`)
