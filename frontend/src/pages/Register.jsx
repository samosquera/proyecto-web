import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'
import { FaEnvelope, FaLock, FaUser, FaPhone, FaBus, FaCalendar } from 'react-icons/fa'
import '../styles/Auth.css'

const Register = () => {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    phone: '',
    dateOfBirth: '',
    password: '',
    confirmPassword: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
  }

  const validateForm = () => {
    if (!formData.username || !formData.email || !formData.phone ||
        !formData.dateOfBirth || !formData.password) {
      setError('Todos los campos son obligatorios')
      return false
    }

    // Validar fecha de nacimiento
    const birthDate = new Date(formData.dateOfBirth)
    const today = new Date()
    if (birthDate >= today) {
      setError('La fecha de nacimiento debe ser una fecha pasada')
      return false
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden')
      return false
    }

    if (formData.password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres')
      return false
    }

    // Validar que la contraseña contenga al menos una letra y un número
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/
    if (!passwordPattern.test(formData.password)) {
      setError('La contraseña debe contener al menos una letra y un número')
      return false
    }

    return true
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!validateForm()) {
      return
    }

    setLoading(true)

    try {
      const { confirmPassword, ...userData } = formData
      await register({
        ...userData,
        role: 'PASSENGER', // Rol por defecto para nuevos registros
      })
      setSuccess(true)
      setTimeout(() => {
        navigate('/login')
      }, 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrarse')
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <Alert
            type="success"
            message="Registro exitoso. Redirigiendo al inicio de sesión..."
          />
        </div>
      </div>
    )
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <FaBus className="auth-logo" />
          <h1>BERS Transport</h1>
          <p>Crea tu cuenta</p>
        </div>

        {error && <Alert type="error" message={error} />}

        <form onSubmit={handleSubmit} className="auth-form">
          <Input
            label="Nombre de usuario"
            type="text"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="Juan Pérez"
            required
            icon={<FaUser />}
          />

          <Input
            label="Email"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="tu@email.com"
            required
            icon={<FaEnvelope />}
          />

          <Input
            label="Teléfono"
            type="tel"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            placeholder="+1234567890"
            required
            icon={<FaPhone />}
          />

          <Input
            label="Fecha de Nacimiento"
            type="date"
            name="dateOfBirth"
            value={formData.dateOfBirth}
            onChange={handleChange}
            required
            max={new Date().toISOString().split('T')[0]}
            icon={<FaCalendar />}
          />

          <Input
            label="Contraseña"
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
            icon={<FaLock />}
          />

          <Input
            label="Confirmar Contraseña"
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="••••••••"
            required
            icon={<FaLock />}
          />

          <Button type="submit" fullWidth loading={loading}>
            Registrarse
          </Button>
        </form>

        <div className="auth-footer">
          <p>
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="auth-link">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Register
