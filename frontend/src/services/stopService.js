import api from './api'

const stopService = {
  // Obtener todas las paradas (público)
  getAllStops: async () => {
    const response = await api.get('/api/v1/stops/all')
    return response.data
  },

  // Obtener parada por ID (público)
  getStopById: async (id) => {
    const response = await api.get(`/api/v1/stops/${id}`)
    return response.data
  },

  // Obtener paradas por ruta (público)
  getStopsByRoute: async (routeId) => {
    const response = await api.get(`/api/v1/stops/route/${routeId}`)
    return response.data
  },

  // Buscar paradas por nombre (público)
  searchStopsByName: async (name) => {
    const response = await api.get('/api/v1/stops/search', {
      params: { name }
    })
    return response.data
  },

  // Obtener destinos disponibles desde una parada (público)
  getAvailableDestinations: async (fromStopId) => {
    const response = await api.get(`/api/v1/stops/available-destinations/${fromStopId}`)
    return response.data
  },

  // Crear parada (DISPATCHER/ADMIN)
  createStop: async (stopData) => {
    const response = await api.post('/api/v1/stops/create', stopData)
    return response.data
  },

  // Crear paradas en batch (DISPATCHER/ADMIN)
  createStopsBatch: async (stopsData) => {
    const response = await api.post('/api/v1/stops/batch-create', stopsData)
    return response.data
  },

  // Actualizar parada (DISPATCHER/ADMIN)
  updateStop: async (id, stopData) => {
    const response = await api.put(`/api/v1/stops/update/${id}`, stopData)
    return response.data
  },

  // Eliminar parada (ADMIN)
  deleteStop: async (id) => {
    const response = await api.delete(`/api/v1/stops/delete/${id}`)
    return response.data
  }
}

export default stopService
