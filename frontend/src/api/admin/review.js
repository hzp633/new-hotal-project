import request from '../request'

export const getReviewPage = (params) => request.get('/admin/review/page', { params })
export const updateReviewStatus = (id, status) => request.put(`/admin/review/${id}/status`, null, { params: { status } })
export const replyReview = (id, data) => request.post(`/admin/review/${id}/reply`, data)
