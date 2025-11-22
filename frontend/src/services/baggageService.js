import api from './api'

const baggageService = {
  // Get all baggage
  getAllBaggage: async () => {
    const response = await api.get('/api/v1/baggage/all')
    return response.data
  },

  // Get baggage by ID
  getBaggageById: async (id) => {
    const response = await api.get(`/api/v1/baggage/${id}`)
    return response.data
  },

  // Get baggage by tag code
  getBaggageByTag: async (tagCode) => {
    const response = await api.get(`/api/v1/baggage/tag/${tagCode}`)
    return response.data
  },

  // Get baggage by ticket
  getBaggageByTicket: async (ticketId) => {
    const response = await api.get(`/api/v1/baggage/ticket/${ticketId}`)
    return response.data
  },

  // Get baggage by trip
  getBaggageByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/baggage/trip/${tripId}`)
    return response.data
  },

  // Calculate baggage fee
  calculateBaggageFee: async (weightKg) => {
    const response = await api.get('/api/v1/baggage/calculate-fee', {
      params: { weightKg },
    })
    return response.data
  },

  // Create baggage (CLERK, ADMIN)
  createBaggage: async (data) => {
    const response = await api.post('/api/v1/baggage/create', data)
    return response.data
  },

  // Update baggage (CLERK, ADMIN)
  updateBaggage: async (id, data) => {
    const response = await api.put(`/api/v1/baggage/update/${id}`, data)
    return response.data
  },

  // Delete baggage (ADMIN)
  deleteBaggage: async (id) => {
    await api.delete(`/api/v1/baggage/delete/${id}`)
  },
}

export default baggageService
