import api from './api'

const seatService = {
  // Crear asiento
  createSeat: async (seatData) => {
    const response = await api.post('/api/v1/seats/create', seatData)
    return response.data
  },

  // Obtener todos los asientos
  getAllSeats: async () => {
    const response = await api.get('/api/v1/seats/all')
    return response.data
  },

  // Crear múltiples asientos (DISPATCHER/ADMIN)
  batchCreateSeats: async (seatsData) => {
    const response = await api.post('/api/v1/seats/batch-create', seatsData)
    return response.data
  },

  // Obtener asiento por ID
  getSeatById: async (id) => {
    const response = await api.get(`/api/v1/seats/${id}`)
    return response.data
  },

  // Actualizar asiento
  updateSeat: async (id, seatData) => {
    const response = await api.put(`/api/v1/seats/update/${id}`, seatData)
    return response.data
  },

  // Eliminar asiento (ADMIN)
  deleteSeat: async (id) => {
    const response = await api.delete(`/api/v1/seats/delete/${id}`)
    return response.data
  },

  // Obtener asientos de un bus
  getSeatsByBus: async (busId) => {
    const response = await api.get(`/api/v1/seats/bus/${busId}`)
    return response.data
  },

  // Obtener asientos con estado de ocupación para un viaje
  getFullSeatsAndHolds: async (tripId) => {
    const response = await api.get(`/api/v1/seats/${tripId}/full-seats-and-holds`)
    return response.data
  },

  // Obtener asientos disponibles por segmento (fromStop, toStop)
  getSeatsBySegment: async (tripId, fromStopId, toStopId) => {
    const response = await api.get(`/api/v1/seats/trips/${tripId}/seats`, {
      params: {
        fromStopId,
        toStopId,
      },
    })
    return response.data
  },

  // Obtener asiento específico de bus
  getSeatByBusAndNumber: async (busId, number) => {
    const response = await api.get(`/api/v1/seats/bus/${busId}/number/${number}`)
    return response.data
  },

  // Obtener asientos por tipo
  getSeatsByType: async (busId, type) => {
    const response = await api.get(`/api/v1/seats/bus/${busId}/type/${type}`)
    return response.data
  },

  // Contar asientos de un bus
  countSeatsByBus: async (busId) => {
    const response = await api.get(`/api/v1/seats/bus/${busId}/count`)
    return response.data
  },
}

export default seatService
