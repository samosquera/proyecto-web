import axios from 'axios'
import authService from './authService'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor para agregar el token JWT a todas las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Variable para evitar múltiples intentos de refresh simultáneos
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

// Interceptor mejorado para manejar errores de autenticación con refresh token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Logging detallado del error para diagnóstico
    console.error('🔴 API Error:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: originalRequest?.url,
      method: originalRequest?.method,
      message: error.response?.data?.message || error.message,
      data: error.response?.data,
    })

    // Si es error 401 y no es un reintento
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Si es una petición a /auth/refresh, no intentar refrescar de nuevo
      if (originalRequest.url?.includes('/auth/refresh')) {
        console.error('❌ Refresh token expirado o inválido. Cerrando sesión...')
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/'
        return Promise.reject(error)
      }

      // Si ya estamos refrescando el token, agregar a la cola
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return api(originalRequest)
          })
          .catch((err) => {
            return Promise.reject(err)
          })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')

      if (!refreshToken) {
        console.error('❌ No hay refresh token disponible. Cerrando sesión...')
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/'
        return Promise.reject(error)
      }

      try {
        console.log('🔄 Token expirado. Intentando renovar con refresh token...')
        const response = await authService.refreshToken()

        const newAccessToken = response.accessToken
        localStorage.setItem('token', newAccessToken)

        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken)
        }

        console.log('✅ Token renovado exitosamente')

        // Actualizar el header de la petición original
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`

        // Procesar la cola de peticiones que esperaban el nuevo token
        processQueue(null, newAccessToken)

        // Reintentar la petición original
        return api(originalRequest)
      } catch (refreshError) {
        console.error('❌ Error al renovar token:', refreshError)
        processQueue(refreshError, null)

        // Solo cerrar sesión si el refresh falló
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.href = '/'

        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // Si es otro tipo de error (400, 403, 500, etc.), solo rechazar
    return Promise.reject(error)
  }
)

export default api
