import request from '../request'

export const updateHotel = (data) => request.put('/admin/hotel', data)
