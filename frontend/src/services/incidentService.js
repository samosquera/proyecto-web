import api from './api'

const incidentService = {
  // Get all incidents
  getAllIncidents: async () => {
    const response = await api.get('/api/v1/incidents/all')
    return response.data
  },

  // Get incident by ID
  getIncidentById: async (id) => {
    const response = await api.get(`/api/v1/incidents/${id}`)
    return response.data
  },

  // Get incidents by entity (TRIP, TICKET, PARCEL)
  getIncidentsByEntity: async (entityType, entityId) => {
    const response = await api.get('/api/v1/incidents/entity', {
      params: { entityType, entityId },
    })
    return response.data
  },

  // Get incidents by type (SECURITY, DELIVERY_FAIL, OVERBOOK, etc.)
  getIncidentsByType: async (type) => {
    const response = await api.get(`/api/v1/incidents/type/${type}`)
    return response.data
  },

  // Get incidents by reporter
  getIncidentsByReporter: async (reportedById) => {
    const response = await api.get(`/api/v1/incidents/reported-by/${reportedById}`)
    return response.data
  },

  // Get incidents by date range
  getIncidentsByDateRange: async (startDate, endDate) => {
    const response = await api.get('/api/v1/incidents/date-range', {
      params: { start: startDate, end: endDate },
    })
    return response.data
  },

  // Create incident
  createIncident: async (data) => {
    const response = await api.post('/api/v1/incidents/create', data)
    return response.data
  },

  // Update incident note
  updateIncident: async (id, note) => {
    const response = await api.put(`/api/v1/incidents/update/${id}`, { note })
    return response.data
  },

  // Delete incident (ADMIN)
  deleteIncident: async (id) => {
    await api.delete(`/api/v1/incidents/delete/${id}`)
  },

  // Count incidents by type since a date
  countIncidentsByType: async (type, sinceDate) => {
    const response = await api.get('/api/v1/incidents/count', {
      params: { type, since: sinceDate },
    })
    return response.data
  },
}

export default incidentService
