import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Modal from './Modal'
import Button from './Button'
import Input from './Input'
import Alert from './Alert'
import { FaEnvelope, FaLock, FaPhone } from 'react-icons/fa'
import '../styles/AuthModal.css'

const LoginModal = ({ isOpen, onClose, onSwitchToRegister }) => {
  const navigate = useNavigate()
  const { login, loginWithPhone, error: authError } = useAuth()
  const [loginMethod, setLoginMethod] = useState('email') // 'email' o 'phone'
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    phoneNumber: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      if (loginMethod === 'email') {
        await login({ email: formData.email, password: formData.password })
      } else {
        await loginWithPhone({ phoneNumber: formData.phoneNumber, password: formData.password })
      }

      setSuccess('Inicio de sesión exitoso')
      setTimeout(() => {
        onClose()
        // Navegar según el rol del usuario
        const user = JSON.parse(localStorage.getItem('user'))
        if (user?.role === 'ADMIN') {
          navigate('/admin/dashboard')
        } else if (user?.role === 'DISPATCHER') {
          navigate('/dispatcher/dashboard')
        } else if (user?.role === 'DRIVER') {
          navigate('/driver/dashboard')
        } else if (user?.role === 'CLERK') {
          navigate('/clerk/dashboard')
        } else {
          navigate('/')
        }
      }, 1000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setFormData({
      email: '',
      password: '',
      phoneNumber: '',
    })
    setError('')
    setSuccess('')
  }

  const handleClose = () => {
    resetForm()
    onClose()
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Iniciar Sesión" size="medium">
      <div className="auth-modal-content">
        {(error || authError) && (
          <Alert type="error" message={error || authError} onClose={() => setError('')} />
        )}
        {success && <Alert type="success" message={success} />}

        {/* Selector de método de login */}
        <div className="login-method-selector">
          <button
            type="button"
            className={`method-btn ${loginMethod === 'email' ? 'active' : ''}`}
            onClick={() => setLoginMethod('email')}
          >
            <FaEnvelope /> Email
          </button>
          <button
            type="button"
            className={`method-btn ${loginMethod === 'phone' ? 'active' : ''}`}
            onClick={() => setLoginMethod('phone')}
          >
            <FaPhone /> Teléfono
          </button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {loginMethod === 'email' ? (
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
          ) : (
            <Input
              label="Número de Teléfono"
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="+591 12345678"
              required
              icon={<FaPhone />}
            />
          )}

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

          <Button type="submit" fullWidth loading={loading}>
            Iniciar Sesión
          </Button>
        </form>

        <div className="auth-modal-footer">
          <p>
            ¿No tienes cuenta?{' '}
            <button
              type="button"
              className="auth-link-btn"
              onClick={() => {
                handleClose()
                onSwitchToRegister()
              }}
            >
              Regístrate aquí
            </button>
          </p>
        </div>
      </div>
    </Modal>
  )
}

export default LoginModal
