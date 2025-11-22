import api from './api'

const busService = {
  // Crear autobús (DISPATCHER/ADMIN)
  createBus: async (busData) => {
    const response = await api.post('/api/v1/buses/create', busData)
    return response.data
  },

  // Obtener todos los autobuses
  getAllBuses: async () => {
    const response = await api.get('/api/v1/buses/all')
    return response.data
  },

  // Obtener autobús por ID
  getBusById: async (id) => {
    const response = await api.get(`/api/v1/buses/${id}`)
    return response.data
  },

  // Obtener autobús con asientos
  getBusWithSeats: async (id) => {
    const response = await api.get(`/api/v1/buses/${id}/with-seats`)
    return response.data
  },

  // Obtener autobús por placa
  getBusByPlate: async (plate) => {
    const response = await api.get(`/api/v1/buses/plate/${plate}`)
    return response.data
  },

  // Actualizar autobús
  updateBus: async (id, busData) => {
    const response = await api.put(`/api/v1/buses/update/${id}`, busData)
    return response.data
  },

  // Eliminar autobús (ADMIN)
  deleteBus: async (id) => {
    const response = await api.delete(`/api/v1/buses/delete/${id}`)
    return response.data
  },

  // Obtener autobuses por estado
  getBusesByStatus: async (status) => {
    const response = await api.get(`/api/v1/buses/status/${status}`)
    return response.data
  },

  // Obtener autobuses disponibles
  getAvailableBuses: async () => {
    const response = await api.get('/api/v1/buses/available')
    return response.data
  },

  // Verificar si placa existe
  plateExists: async (plate) => {
    const response = await api.get(`/api/v1/buses/plate/${plate}/exists`)
    return response.data
  },

  // Cambiar estado del autobús
  changeBusStatus: async (id, status) => {
    const response = await api.patch(`/api/v1/buses/${id}/status`, null, {
      params: { status }
    })
    return response.data
  },
}

export default busService
