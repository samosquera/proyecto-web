import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'
import { FaEnvelope, FaLock, FaBus } from 'react-icons/fa'
import '../styles/Auth.css'

const Login = () => {
  const navigate = useNavigate()
  const { login, error: authError } = useAuth()
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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
    setLoading(true)

    try {
      await login(formData)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <FaBus className="auth-logo" />
          <h1>BERS Transport</h1>
          <p>Inicia sesión en tu cuenta</p>
        </div>

        {(error || authError) && <Alert type="error" message={error || authError} />}

        <form onSubmit={handleSubmit} className="auth-form">
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
            label="Contraseña"
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            required
            icon={<FaLock />}
          />

          <div className="auth-options">
            <Link to="/forgot-password" className="forgot-password">
              ¿Olvidaste tu contraseña?
            </Link>
          </div>

          <Button type="submit" fullWidth loading={loading}>
            Iniciar Sesión
          </Button>
        </form>

        <div className="auth-footer">
          <p>
            ¿No tienes cuenta?{' '}
            <Link to="/register" className="auth-link">
              Regístrate
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login
