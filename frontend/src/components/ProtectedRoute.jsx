import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import authService from '../services/authService'

const ProtectedRoute = ({ children, roles }) => {
  const { user, isAuthenticated, hasRole } = useAuth()

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  // Si estamos autenticados pero user no está cargado, usar localStorage
  let currentUser = user
  if (!currentUser) {
    currentUser = authService.getCurrentUser()
  }

  // Verificar roles con el usuario actual (del estado o localStorage)
  if (roles && currentUser) {
    const userHasRole = typeof roles === 'string'
      ? currentUser.role === roles
      : roles.includes(currentUser.role)

    if (!userHasRole) {
      return <Navigate to="/dashboard" replace />
    }
  }

  return children
}

export default ProtectedRoute
