import api from './api'
import { extractRoleFromToken } from '../utils/jwtHelper'

const authService = {
  // Registro de nuevo usuario
  register: async (userData) => {
    const response = await api.post('/api/auth/register', {
      username: userData.username,
      email: userData.email,
      phone: userData.phone,
      password: userData.password,
      dateOfBirth: userData.dateOfBirth,
    })
    // AuthResponse: { accessToken, refreshToken, tokenType, expiresIn, user }
    if (response.data.accessToken) {
      localStorage.setItem('token', response.data.accessToken)
      if (response.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken)
      }
      // Agregar el role del token al user object
      const userWithRole = {
        ...response.data.user,
        role: extractRoleFromToken(response.data.accessToken),
      }
      localStorage.setItem('user', JSON.stringify(userWithRole))
      response.data.user = userWithRole
    }
    return response.data
  },

  // Login con email y contraseña
  login: async (credentials) => {
    const response = await api.post('/api/auth/login', {
      email: credentials.email,
      password: credentials.password,
    })
    // AuthResponse: { accessToken, refreshToken, tokenType, expiresIn, user }
    if (response.data.accessToken) {
      localStorage.setItem('token', response.data.accessToken)
      if (response.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken)
      }
      // Agregar el role del token al user object
      const userWithRole = {
        ...response.data.user,
        role: extractRoleFromToken(response.data.accessToken),
      }
      localStorage.setItem('user', JSON.stringify(userWithRole))
      response.data.user = userWithRole
    }
    return response.data
  },

  // Login con teléfono
  loginWithPhone: async (credentials) => {
    const response = await api.post('/api/auth/login/phone', {
      phone: credentials.phoneNumber || credentials.phone, // Soportar ambos nombres
      password: credentials.password,
    })
    // AuthResponse: { accessToken, refreshToken, tokenType, expiresIn, user }
    if (response.data.accessToken) {
      localStorage.setItem('token', response.data.accessToken)
      if (response.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken)
      }
      // Agregar el role del token al user object
      const userWithRole = {
        ...response.data.user,
        role: extractRoleFromToken(response.data.accessToken),
      }
      localStorage.setItem('user', JSON.stringify(userWithRole))
      response.data.user = userWithRole
    }
    return response.data
  },

  // Logout
  logout: async () => {
    try {
      const token = localStorage.getItem('token')
      const refreshToken = localStorage.getItem('refreshToken')

      if (token) {
        await api.post('/api/auth/logout', {
          accessToken: token,
          refreshToken: refreshToken,
        })
      }
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  },

  // Obtener información del usuario autenticado
  getMe: async () => {
    const response = await api.get('/api/auth/me')
    // UserInfo: { id, username, email, phone, status }
    // Agregar el role desde el token
    const token = localStorage.getItem('token')
    if (token) {
      const userWithRole = {
        ...response.data,
        role: extractRoleFromToken(token),
      }
      // Actualizar el localStorage con la info más reciente
      localStorage.setItem('user', JSON.stringify(userWithRole))
      return userWithRole
    }
    return response.data
  },

  // Cambiar contraseña
  changePassword: async (passwordData) => {
    const response = await api.post('/api/auth/change-password', {
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword,
      confirmPassword: passwordData.confirmPassword,
    })
    // MessageResponse: { message, success }
    return response.data
  },

  // Solicitar reset de contraseña
  forgotPassword: async (email) => {
    const response = await api.post('/api/auth/forgot-password', { email })
    // MessageResponse: { message, success }
    return response.data
  },

  // Resetear contraseña con token
  resetPassword: async (resetData) => {
    const response = await api.post('/api/auth/reset-password', {
      token: resetData.token,
      newPassword: resetData.newPassword,
      confirmPassword: resetData.confirmPassword,
    })
    // MessageResponse: { message, success }
    return response.data
  },

  // Validar token
  validateToken: async (token) => {
    const response = await api.post('/api/auth/validate', null, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    // TokenValidationResponse: { valid, username, role, userId, expiresIn }
    return response.data
  },

  // Refrescar token
  refreshToken: async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    const response = await api.post('/api/auth/refresh', {
      refreshToken: refreshToken,
    })
    // AuthResponse: { accessToken, refreshToken, tokenType, expiresIn, user }
    if (response.data.accessToken) {
      localStorage.setItem('token', response.data.accessToken)
      if (response.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.refreshToken)
      }
      if (response.data.user) {
        // Agregar el role del token al user object
        const userWithRole = {
          ...response.data.user,
          role: extractRoleFromToken(response.data.accessToken),
        }
        localStorage.setItem('user', JSON.stringify(userWithRole))
        response.data.user = userWithRole
      }
    }
    return response.data
  },

  // Obtener usuario del localStorage
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user')
    if (!userStr) return null

    const user = JSON.parse(userStr)

    // Si el user no tiene role, intentar extraerlo del token
    if (!user.role) {
      const token = localStorage.getItem('token')
      if (token) {
        const role = extractRoleFromToken(token)
        if (role) {
          user.role = role
          localStorage.setItem('user', JSON.stringify(user))
        }
      }
    }

    return user
  },

  // Verificar si el usuario está autenticado
  isAuthenticated: () => {
    return !!localStorage.getItem('token')
  },
}

export default authService
