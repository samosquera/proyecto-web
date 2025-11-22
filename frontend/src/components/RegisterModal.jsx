import React, { useState } from 'react'
import Modal from './Modal'
import Button from './Button'
import Input from './Input'
import Alert from './Alert'
import { FaEnvelope, FaLock, FaUser, FaPhone, FaCalendar } from 'react-icons/fa'
import { useAuth } from '../context/AuthContext'
import '../styles/AuthModal.css'

const RegisterModal = ({ isOpen, onClose, onSwitchToLogin }) => {
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
    if (
      !formData.username ||
      !formData.email ||
      !formData.phone ||
      !formData.dateOfBirth ||
      !formData.password
    ) {
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
        handleClose()
        onSwitchToLogin()
      }, 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrarse')
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      phone: '',
      dateOfBirth: '',
      password: '',
      confirmPassword: '',
    })
    setError('')
    setSuccess(false)
  }

  const handleClose = () => {
    resetForm()
    onClose()
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Crear Cuenta" size="medium">
      <div className="auth-modal-content">
        {error && <Alert type="error" message={error} onClose={() => setError('')} />}
        {success && <Alert type="success" message="Registro exitoso. Ahora puedes iniciar sesión." />}

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
            placeholder="+591 12345678"
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
            helperText="Mínimo 8 caracteres, incluir letras y números"
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

          <Button type="submit" fullWidth loading={loading} disabled={success}>
            Registrarse
          </Button>
        </form>

        <div className="auth-modal-footer">
          <p>
            ¿Ya tienes cuenta?{' '}
            <button
              type="button"
              className="auth-link-btn"
              onClick={() => {
                handleClose()
                onSwitchToLogin()
              }}
            >
              Inicia sesión aquí
            </button>
          </p>
        </div>
      </div>
    </Modal>
  )
}

export default RegisterModal
