import api from './api'

const routeService = {
  // Obtener todas las rutas (público)
  getAllRoutes: async () => {
    const response = await api.get('/api/v1/routes/all')
    return response.data
  },

  // Obtener orígenes disponibles (público)
  getOrigins: async () => {
    const response = await api.get('/api/v1/routes/origins')
    return response.data
  },

  // Obtener destinos por origen (público)
  getDestinationsByOrigin: async (origin) => {
    const response = await api.get('/api/v1/routes/destinations', {
      params: { origin }
    })
    return response.data
  },

  // Obtener ruta por ID
  getRouteById: async (id) => {
    const response = await api.get(`/api/v1/routes/${id}`)
    return response.data
  },

  // Obtener paradas de una ruta
  getRouteStops: async (id) => {
    const response = await api.get(`/api/v1/routes/${id}/stops`)
    return response.data
  },

  // Buscar rutas por origen y destino
  searchRoutes: async (params) => {
    const response = await api.get('/api/v1/routes/search', { params })
    return response.data
  },

  // Obtener ruta por código
  getRouteByCode: async (code) => {
    const response = await api.get(`/api/v1/routes/code/${code}`)
    return response.data
  },

  // Obtener ruta con paradas
  getRouteWithStops: async (id) => {
    const response = await api.get(`/api/v1/routes/${id}/with-stops`)
    return response.data
  },

  // Crear ruta (DISPATCHER/ADMIN)
  createRoute: async (routeData) => {
    const response = await api.post('/api/v1/routes/create', routeData)
    return response.data
  },

  // Actualizar ruta (DISPATCHER/ADMIN)
  updateRoute: async (id, routeData) => {
    const response = await api.put(`/api/v1/routes/update/${id}`, routeData)
    return response.data
  },

  // Eliminar ruta (ADMIN)
  deleteRoute: async (id) => {
    const response = await api.delete(`/api/v1/routes/delete/${id}`)
    return response.data
  },

  // Verificar si código existe
  codeExists: async (code) => {
    const response = await api.get(`/api/v1/routes/code/${code}/exists`)
    return response.data
  },
}

export default routeService
