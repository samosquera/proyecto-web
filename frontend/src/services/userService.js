import api from './api'

const userService = {
  // Obtener mi perfil
  getMyProfile: async () => {
    const response = await api.get('/api/v1/users/me')
    return response.data
  },

  // Actualizar mi perfil
  updateMyProfile: async (userData) => {
    const response = await api.put('/api/v1/users/update-me', userData)
    return response.data
  },

  // Cambiar mi contraseña
  changeMyPassword: async (passwordData) => {
    const response = await api.patch('/api/v1/users/me/password', passwordData)
    return response.data
  },

  // Crear usuario (ADMIN)
  createUser: async (userData) => {
    const response = await api.post('/api/v1/users/create', userData)
    return response.data
  },

  // Actualizar usuario (ADMIN/DISPATCHER)
  updateUser: async (id, userData) => {
    const response = await api.put(`/api/v1/users/update/${id}`, userData)
    return response.data
  },

  // Obtener usuario por ID
  getUserById: async (id) => {
    const response = await api.get(`/api/v1/users/${id}`)
    return response.data
  },

  // Obtener usuario por email
  getUserByEmail: async (email) => {
    const response = await api.get(`/api/v1/users/email/${email}`)
    return response.data
  },

  // Obtener usuario por teléfono
  getUserByPhone: async (phone) => {
    const response = await api.get(`/api/v1/users/phone/${phone}`)
    return response.data
  },

  // Obtener todos los usuarios (ADMIN/DISPATCHER)
  getAllUsers: async () => {
    const response = await api.get('/api/v1/users/all-users')
    return response.data
  },

  // Obtener usuarios por rol
  getUsersByRole: async (role) => {
    const response = await api.get(`/api/v1/users/role/${role}`)
    return response.data
  },

  // Obtener usuarios por rol y estado
  getUsersByRoleAndStatus: async (role, status) => {
    const response = await api.get(`/api/v1/users/role/${role}/status/${status}`)
    return response.data
  },

  // Eliminar usuario (ADMIN)
  deleteUser: async (id) => {
    const response = await api.delete(`/api/v1/users/delete/${id}`)
    return response.data
  },

  // Cambiar estado del usuario
  changeUserStatus: async (id, status) => {
    const response = await api.patch(`/api/v1/users/${id}/status`, null, {
      params: { status }
    })
    return response.data
  },

  // Verificar si email existe
  emailExists: async (email) => {
    const response = await api.get(`/api/v1/users/exists/email/${email}`)
    return response.data
  },

  // Verificar si teléfono existe
  phoneExists: async (phone) => {
    const response = await api.get(`/api/v1/users/exists/phone/${phone}`)
    return response.data
  },

  // Verificar disponibilidad de email y teléfono
  checkAvailability: async (data) => {
    const response = await api.post('/api/v1/users/check', data)
    return response.data
  },

  // Obtener usuarios activos por rol
  getActiveUsersByRole: async (role) => {
    const response = await api.get(`/api/v1/users/role/${role}/active`)
    return response.data
  },

  // Actualizar perfil completo
  updateCompleteProfile: async (userData) => {
    const response = await api.put('/api/v1/users/me/complete', userData)
    return response.data
  },
}

export default userService
