import api from './api'

const fareRuleService = {
  // Get all fare rules
  getAllFareRules: async () => {
    const response = await api.get('/api/v1/fare-rules/all')
    return response.data
  },

  // Get fare rule by ID
  getFareRuleById: async (id) => {
    const response = await api.get(`/api/v1/fare-rules/${id}`)
    return response.data
  },

  // Get fare rules by route
  getFareRulesByRoute: async (routeId) => {
    const response = await api.get(`/api/v1/fare-rules/route/${routeId}`)
    return response.data
  },

  // Get fare rule for specific segment
  getFareRuleForSegment: async (routeId, fromStopId, toStopId) => {
    const response = await api.get('/api/v1/fare-rules/segment', {
      params: { routeId, fromStopId, toStopId },
    })
    return response.data
  },

  // Get dynamic fare rules by route
  getDynamicFareRules: async (routeId) => {
    const response = await api.get(`/api/v1/fare-rules/route/${routeId}/dynamic`)
    return response.data
  },

  // Calculate dynamic price
  calculateDynamicPrice: async (fareRuleId, occupancyRate) => {
    const response = await api.get(`/api/v1/fare-rules/${fareRuleId}/calculate-dynamic-price`, {
      params: { occupancyRate },
    })
    return response.data
  },

  // Create fare rule (DISPATCHER, ADMIN)
  createFareRule: async (data) => {
    const response = await api.post('/api/v1/fare-rules/create', data)
    return response.data
  },

  // Update fare rule (DISPATCHER, ADMIN)
  updateFareRule: async (id, data) => {
    const response = await api.put(`/api/v1/fare-rules/update/${id}`, data)
    return response.data
  },

  // Delete fare rule (ADMIN)
  deleteFareRule: async (id) => {
    await api.delete(`/api/v1/fare-rules/delete/${id}`)
  },
}

export default fareRuleService
