import api from './api'

const seatHoldService = {
  // Get all seat holds
  getAllSeatHolds: async () => {
    const response = await api.get('/api/v1/seat-holds/all')
    return response.data
  },

  // Get seat hold by ID
  getSeatHoldById: async (id) => {
    const response = await api.get(`/api/v1/seat-holds/${id}`)
    return response.data
  },

  // Get seat holds by trip
  getSeatHoldsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/seat-holds/trip/${tripId}`)
    return response.data
  },

  // Get seat holds by user
  getSeatHoldsByUser: async (userId) => {
    const response = await api.get(`/api/v1/seat-holds/user/${userId}`)
    return response.data
  },

  // Get my seat holds (current user)
  getMySeatHolds: async () => {
    const response = await api.get('/api/v1/seat-holds/me')
    return response.data
  },

  // Get active seat holds by trip
  getActiveSeatHoldsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/seat-holds/trip/${tripId}/active`)
    return response.data
  },

  // Create seat hold (reserve seat)
  createSeatHold: async (data) => {
    const response = await api.post('/api/v1/seat-holds/create', data)
    return response.data
  },

  // Update seat hold status
  updateSeatHold: async (id, status) => {
    const response = await api.put(`/api/v1/seat-holds/update/${id}`, { status })
    return response.data
  },

  // Release seat hold (cancel reservation)
  releaseSeatHold: async (id) => {
    const response = await api.post(`/api/v1/seat-holds/${id}/release`)
    return response.data
  },

  // Convert hold to ticket
  convertHoldToTicket: async (id) => {
    const response = await api.post(`/api/v1/seat-holds/${id}/convert`)
    return response.data
  },

  // Delete seat hold (ADMIN)
  deleteSeatHold: async (id) => {
    await api.delete(`/api/v1/seat-holds/delete/${id}`)
  },

  // Expire old holds manually
  expireOldHolds: async () => {
    const response = await api.post('/api/v1/seat-holds/expire')
    return response.data
  },
}

export default seatHoldService
