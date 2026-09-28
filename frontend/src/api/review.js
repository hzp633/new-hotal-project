import request from './request'

export const createReview = (data) => request.post('/review', data)
export const getMyReviews = () => request.get('/review/list')
