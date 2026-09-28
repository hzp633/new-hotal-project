import request from '../request'

export const getDashboardSummary = () => request.get('/admin/dashboard/summary')
