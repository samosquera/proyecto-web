import api from './api'

const assignmentService = {
  // Crear asignación (DISPATCHER/ADMIN)
  createAssignment: async (assignmentData) => {
    const response = await api.post('/api/v1/assignments/create', assignmentData)
    return response.data
  },

  // Obtener todas las asignaciones
  getAllAssignments: async () => {
    const response = await api.get('/api/v1/assignments/all')
    return response.data
  },

  // Obtener asignación por ID
  getAssignmentById: async (id) => {
    const response = await api.get(`/api/v1/assignments/${id}`)
    return response.data
  },

  // Actualizar asignación (DISPATCHER/ADMIN)
  updateAssignment: async (id, assignmentData) => {
    const response = await api.put(`/api/v1/assignments/update/${id}`, assignmentData)
    return response.data
  },

  // Obtener asignación por viaje
  getAssignmentByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/assignments/trip/${tripId}`)
    return response.data
  },

  // Obtener asignaciones de un conductor
  getAssignmentsByDriver: async (driverId) => {
    const response = await api.get(`/api/v1/assignments/driver/${driverId}`)
    return response.data
  },

  // Obtener asignaciones activas de un conductor
  getActiveAssignmentsByDriver: async (driverId) => {
    const response = await api.get(`/api/v1/assignments/driver/${driverId}/active`)
    return response.data
  },

  // Obtener asignaciones por fecha
  getAssignmentsByDriverAndDate: async (driverId, date) => {
    const response = await api.get(`/api/v1/assignments/driver/${driverId}/date`, {
      params: { date }
    })
    return response.data
  },

  // Obtener asignaciones de un dispatcher
  getAssignmentsByDispatcher: async (dispatcherId) => {
    const response = await api.get(`/api/v1/assignments/dispatcher/${dispatcherId}`)
    return response.data
  },

  // Aprobar checklist
  approveChecklist: async (id) => {
    const response = await api.post(`/api/v1/assignments/${id}/approve-checklist`)
    return response.data
  },

  // Verificar si un viaje tiene asignación
  hasAssignment: async (tripId) => {
    const response = await api.get(`/api/v1/assignments/trip/${tripId}/has-assignment`)
    return response.data
  },
}

export default assignmentService
