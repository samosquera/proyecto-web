import api from './api'

const overbookingService = {
  // Request overbooking (CLERK, ADMIN)
  requestOverbooking: async (tripId, ticketId, reason) => {
    const response = await api.post('/api/v1/overbooking/request', {
      tripId,
      ticketId,
      reason,
    })
    return response.data
  },

  // Approve overbooking (DISPATCHER)
  approveOverbooking: async (id, notes = '') => {
    const response = await api.post(`/api/v1/overbooking/${id}/approve`, {
      notes,
    })
    return response.data
  },

  // Reject overbooking (DISPATCHER)
  rejectOverbooking: async (id, reason) => {
    const response = await api.post(`/api/v1/overbooking/${id}/reject`, {
      reason,
    })
    return response.data
  },

  // Get pending requests (DISPATCHER, ADMIN)
  getPendingRequests: async () => {
    const response = await api.get('/api/v1/overbooking/pending')
    return response.data
  },

  // Get requests by trip (DISPATCHER, ADMIN)
  getRequestsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/overbooking/trip/${tripId}`)
    return response.data
  },

  // Get requests by status (DISPATCHER, ADMIN)
  getRequestsByStatus: async (status) => {
    const response = await api.get(`/api/v1/overbooking/status/${status}`)
    return response.data
  },

  // Get request by ID (DISPATCHER, ADMIN)
  getRequestById: async (id) => {
    const response = await api.get(`/api/v1/overbooking/${id}`)
    return response.data
  },

  // Check if can overbook (CLERK, ADMIN)
  canOverbook: async (tripId) => {
    const response = await api.get(`/api/v1/overbooking/trip/${tripId}/can-overbook`)
    return response.data
  },

  // Get current occupancy rate (CLERK, DISPATCHER, ADMIN)
  getOccupancyRate: async (tripId) => {
    const response = await api.get(`/api/v1/overbooking/trip/${tripId}/occupancy`)
    return response.data
  },
}

export default overbookingService
