import React, { createContext, useState, useContext, useEffect } from 'react'
import authService from '../services/authService'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    // Verificar si hay un usuario en localStorage al cargar
    const storedUser = authService.getCurrentUser()
    const token = localStorage.getItem('token')

    if (storedUser && token) {
      setUser(storedUser)
    }
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    try {
      setError(null)
      const data = await authService.login(credentials)
      setUser(data.user)
      return data
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al iniciar sesión'
      setError(errorMessage)
      throw err
    }
  }

  const loginWithPhone = async (credentials) => {
    try {
      setError(null)
      const data = await authService.loginWithPhone(credentials)
      setUser(data.user)
      return data
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al iniciar sesión'
      setError(errorMessage)
      throw err
    }
  }

  const register = async (userData) => {
    try {
      setError(null)
      const data = await authService.register(userData)
      return data
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al registrarse'
      setError(errorMessage)
      throw err
    }
  }

  const logout = async () => {
    try {
      await authService.logout()
    } finally {
      setUser(null)
    }
  }

  const updateUser = (userData) => {
    setUser(userData)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  const hasRole = (roles) => {
    if (!user) return false
    if (typeof roles === 'string') {
      return user.role === roles
    }
    return roles.includes(user.role)
  }

  const isAuthenticated = () => {
    // Solo verificar el token, no el estado user
    // El estado user puede tardar en actualizarse después del login
    return authService.isAuthenticated()
  }

  const value = {
    user,
    loading,
    error,
    login,
    loginWithPhone,
    register,
    logout,
    updateUser,
    hasRole,
    isAuthenticated,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de AuthProvider')
  }
  return context
}

export default AuthContext
