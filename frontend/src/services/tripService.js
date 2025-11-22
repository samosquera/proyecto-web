import api from './api'

const tripService = {
  // Crear viaje (DISPATCHER/ADMIN)
  createTrip: async (tripData) => {
    const response = await api.post('/api/v1/trips/create', tripData)
    return response.data
  },

  // Filtrar viajes por origen, destino y fecha
  filterTrips: async (params) => {
    const response = await api.get('/api/v1/trips/filter', { params })
    return response.data
  },

  // Obtener viaje por ID
  getTripById: async (id) => {
    const response = await api.get(`/api/v1/trips/search/${id}`)
    return response.data
  },

  // Obtener detalles del viaje
  getTripDetails: async (id) => {
    const response = await api.get(`/api/v1/trips/${id}/details`)
    return response.data
  },

  // Actualizar viaje (DISPATCHER/ADMIN)
  updateTrip: async (id, tripData) => {
    const response = await api.put(`/api/v1/trips/update/${id}`, tripData)
    return response.data
  },

  // Obtener todos los viajes (DISPATCHER/ADMIN)
  getAllTrips: async () => {
    const response = await api.get('/api/v1/trips/all')
    return response.data
  },

  // Buscar viajes
  searchTrips: async (params) => {
    const response = await api.get('/api/v1/trips/search', { params })
    return response.data
  },

  // Obtener viajes de una ruta
  getTripsByRoute: async (routeId, date) => {
    const response = await api.get(`/api/v1/trips/route/${routeId}`, {
      params: { date }
    })
    return response.data
  },

  // Obtener viajes activos de un bus
  getTripsByBus: async (busId) => {
    const response = await api.get(`/api/v1/trips/bus/${busId}`)
    return response.data
  },

  // Cambiar estado del viaje
  changeTripStatus: async (id, status) => {
    const response = await api.patch(`/api/v1/trips/${id}/status`, null, {
      params: { status }
    })
    return response.data
  },

  // Abrir abordaje
  openBoarding: async (id) => {
    const response = await api.post(`/api/v1/trips/${id}/boarding/open`)
    return response.data
  },

  // Cerrar abordaje
  closeBoarding: async (id) => {
    const response = await api.post(`/api/v1/trips/${id}/boarding/close`)
    return response.data
  },

  // Marcar como partido
  departTrip: async (id) => {
    const response = await api.post(`/api/v1/trips/${id}/depart`)
    return response.data
  },

  // Marcar como llegado
  arriveTrip: async (id) => {
    const response = await api.post(`/api/v1/trips/${id}/arrive`)
    return response.data
  },

  // Cancelar viaje
  cancelTrip: async (id, reason) => {
    const response = await api.post(`/api/v1/trips/${id}/cancel`, { reason })
    return response.data
  },

  // Reactivar viaje
  reactivateTrip: async (id) => {
    const response = await api.post(`/api/v1/trips/${id}/reactivate`)
    return response.data
  },

  // Eliminar viaje (ADMIN)
  deleteTrip: async (id) => {
    const response = await api.delete(`/api/v1/trips/delete/${id}`)
    return response.data
  },

  // Obtener viajes de hoy
  getTodayTrips: async () => {
    const response = await api.get('/api/v1/trips/today')
    return response.data
  },

  // Obtener viajes por estado
  getTripsByStatus: async (status) => {
    const response = await api.get(`/api/v1/trips/status/${status}`)
    return response.data
  },

  // Obtener viajes activos de hoy
  getTodayActiveTrips: async () => {
    const response = await api.get('/api/v1/trips/today/active')
    return response.data
  },

  // Obtener mis viajes (conductor)
  getMyTrips: async () => {
    const response = await api.get('/api/v1/trips/driver/my-trips')
    return response.data
  },

  // Obtener viajes actuales (conductor)
  getCurrentTrips: async () => {
    const response = await api.get('/api/v1/trips/driver/current-trips')
    return response.data
  },

  // Obtener viajes activos asignados (conductor)
  getActiveAssignedTrips: async () => {
    const response = await api.get('/api/v1/trips/driver/active-trips')
    return response.data
  },
}

export default tripService
