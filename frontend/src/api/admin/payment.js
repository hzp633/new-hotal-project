import request from '../request'

export const getRefundPage = (params) => request.get('/admin/payment/refund/page', { params })
export const processRefund = (id, data) => request.post(`/admin/payment/${id}/refund`, data)
