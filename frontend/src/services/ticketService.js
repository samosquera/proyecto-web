import api from './api'

const ticketService = {
  // Crear boleto
  createTicket: async (ticketData) => {
    const response = await api.post('/api/v1/tickets/create', ticketData)
    return response.data
  },

  // Confirmar pago del boleto
  confirmPayment: async (id, paymentData) => {
    const response = await api.post(`/api/v1/tickets/${id}/confirm-payment`, paymentData)
    return response.data
  },

  // Obtener mis boletos
  getMyTickets: async () => {
    const response = await api.get('/api/v1/tickets/my-tickets')
    return response.data
  },

  // Cancelar boleto
  cancelTicket: async (id, reason) => {
    const response = await api.post(`/api/v1/tickets/${id}/cancel`, { reason })
    return response.data
  },

  // Obtener todos los boletos (CLERK/ADMIN)
  getAllTickets: async () => {
    const response = await api.get('/api/v1/tickets/all')
    return response.data
  },

  // Obtener boleto por ID
  getTicketById: async (id) => {
    const response = await api.get(`/api/v1/tickets/${id}`)
    return response.data
  },

  // Obtener detalles del boleto
  getTicketDetails: async (id) => {
    const response = await api.get(`/api/v1/tickets/${id}/details`)
    return response.data
  },

  // Obtener boleto por código QR
  getTicketByQR: async (qrCode) => {
    const response = await api.get(`/api/v1/tickets/qr/${qrCode}`)
    return response.data
  },

  // Obtener boletos de un viaje
  getTicketsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/tickets/trip/${tripId}`)
    return response.data
  },

  // Obtener boletos por viaje y estado
  getTicketsByTripAndStatus: async (tripId, status) => {
    const response = await api.get(`/api/v1/tickets/trip/${tripId}/status/${status}`)
    return response.data
  },

  // Obtener boletos de pasajero
  getTicketsByPassenger: async (passengerId) => {
    const response = await api.get(`/api/v1/tickets/passenger/${passengerId}`)
    return response.data
  },

  // Actualizar boleto
  updateTicket: async (id, ticketData) => {
    const response = await api.put(`/api/v1/tickets/update/${id}`, ticketData)
    return response.data
  },

  // Eliminar boleto (ADMIN)
  deleteTicket: async (id) => {
    const response = await api.delete(`/api/v1/tickets/delete/${id}`)
    return response.data
  },

  // Marcar boleto como usado
  markAsUsed: async (id) => {
    const response = await api.post(`/api/v1/tickets/${id}/used`)
    return response.data
  },

  // Marcar boleto como no-show
  markAsNoShow: async (id) => {
    const response = await api.post(`/api/v1/tickets/${id}/no-show`)
    return response.data
  },

  // Verificar disponibilidad de asiento
  checkAvailability: async (params) => {
    const response = await api.get('/api/v1/tickets/check-availability', { params })
    return response.data
  },

  // Contar boletos vendidos por viaje
  countTicketsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/tickets/trip/${tripId}/count`)
    return response.data
  },
}

export default ticketService
