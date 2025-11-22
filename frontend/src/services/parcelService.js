import api from './api'

const parcelService = {
  // Crear encomienda (CLERK/ADMIN)
  createParcel: async (parcelData) => {
    const response = await api.post('/api/v1/parcels/create', parcelData)
    return response.data
  },

  // Obtener todas las encomiendas
  getAllParcels: async () => {
    const response = await api.get('/api/v1/parcels/all')
    return response.data
  },

  // Obtener encomienda por ID
  getParcelById: async (id) => {
    const response = await api.get(`/api/v1/parcels/${id}`)
    return response.data
  },

  // Obtener encomienda por código
  getParcelByCode: async (code) => {
    const response = await api.get(`/api/v1/parcels/code/${code}`)
    return response.data
  },

  // Obtener encomiendas por teléfono
  getParcelsByPhone: async (phone) => {
    const response = await api.get(`/api/v1/parcels/phone/${phone}`)
    return response.data
  },

  // Obtener encomiendas por estado
  getParcelsByStatus: async (status) => {
    const response = await api.get(`/api/v1/parcels/status/${status}`)
    return response.data
  },

  // Obtener encomiendas de un viaje
  getParcelsByTrip: async (tripId) => {
    const response = await api.get(`/api/v1/parcels/trip/${tripId}`)
    return response.data
  },

  // Actualizar encomienda (CLERK/ADMIN)
  updateParcel: async (id, parcelData) => {
    const response = await api.put(`/api/v1/parcels/update/${id}`, parcelData)
    return response.data
  },

  // Eliminar encomienda (ADMIN)
  deleteParcel: async (id) => {
    const response = await api.delete(`/api/v1/parcels/delete/${id}`)
    return response.data
  },

  // Marcar como en tránsito
  markAsInTransit: async (id, tripId) => {
    const response = await api.post(`/api/v1/parcels/${id}/in-transit`, null, {
      params: { tripId },
    })
    return response.data
  },

  // Marcar como entregado (sin foto)
  markAsDelivered: async (id, otp, photoUrl = null) => {
    const response = await api.post(`/api/v1/parcels/${id}/delivered`, null, {
      params: { otp, photoUrl },
    })
    return response.data
  },

  // Marcar como entregado con foto
  markAsDeliveredWithPhoto: async (id, otp, photoFile) => {
    const formData = new FormData()
    formData.append('otp', otp)
    if (photoFile) {
      formData.append('photo', photoFile)
    }

    const response = await api.post(`/api/v1/parcels/${id}/delivered-with-photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  // Marcar como fallido
  markAsFailed: async (id, reason) => {
    const response = await api.post(`/api/v1/parcels/${id}/failed`, null, {
      params: { reason },
    })
    return response.data
  },

  // Validar OTP
  validateOtp: async (id, otp) => {
    const response = await api.post(`/api/v1/parcels/${id}/validate-otp`, null, {
      params: { otp },
    })
    return response.data
  },

  // Obtener URL de archivo
  getFileUrl: (path) => {
    if (!path) return null
    // path viene como "/uploads/proof-of-delivery/20250118_123456_abc123.jpg"
    const parts = path.split('/')
    if (parts.length >= 3) {
      const directory = parts[parts.length - 2]
      const filename = parts[parts.length - 1]
      return `${api.defaults.baseURL}/api/v1/files/${directory}/${filename}`
    }
    return null
  },
}

export default parcelService
