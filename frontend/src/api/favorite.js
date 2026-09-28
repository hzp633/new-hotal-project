import request from './request'

export const getFavorites = () => request.get('/favorite/list')
export const addFavorite = (roomTypeId) => request.post(`/favorite/${roomTypeId}`)
export const removeFavorite = (roomTypeId) => request.delete(`/favorite/${roomTypeId}`)
export const checkFavorite = (roomTypeId) => request.get(`/favorite/check/${roomTypeId}`)
