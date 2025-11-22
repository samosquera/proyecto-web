import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { FaBus, FaUser, FaSignOutAlt, FaBars, FaTimes } from 'react-icons/fa'
import '../styles/Navbar.css'

const Navbar = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const toggleMenu = () => {
    setMenuOpen(!menuOpen)
  }

  const getRoleBasedLinks = () => {
    if (!user) return []

    const links = [
      { to: '/dashboard', label: 'Dashboard' },
      { to: '/search-trips', label: 'Buscar Viajes' },
    ]

    // Agregar enlaces según el rol
    if (user.role === 'PASSENGER' || user.role === 'CUSTOMER') {
      links.push({ to: '/my-tickets', label: 'Mis Boletos' })
    }

    if (user.role === 'DRIVER') {
      links.push({ to: '/driver/trips', label: 'Mis Viajes' })
    }

    if (user.role === 'CLERK' || user.role === 'ADMIN') {
      links.push({ to: '/clerk/sales', label: 'Ventas' })
    }

    if (user.role === 'DISPATCHER' || user.role === 'ADMIN') {
      links.push({ to: '/dispatcher/trips', label: 'Gestión Viajes' })
    }

    if (user.role === 'ADMIN') {
      links.push({ to: '/admin/users', label: 'Administración' })
    }

    return links
  }

  const roleLabels = {
    ADMIN: 'Administrador',
    DISPATCHER: 'Despachador',
    DRIVER: 'Conductor',
    CLERK: 'Empleado',
    PASSENGER: 'Pasajero',
    CUSTOMER: 'Cliente',
  }

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/dashboard" className="navbar-logo">
          <FaBus className="navbar-icon" />
          <span>BERS Transport</span>
        </Link>

        <button className="menu-toggle" onClick={toggleMenu}>
          {menuOpen ? <FaTimes /> : <FaBars />}
        </button>

        <div className={`navbar-menu ${menuOpen ? 'active' : ''}`}>
          <ul className="navbar-links">
            {getRoleBasedLinks().map((link) => (
              <li key={link.to}>
                <Link to={link.to} onClick={() => setMenuOpen(false)}>
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>

          <div className="navbar-user">
            <div className="user-info">
              <FaUser className="user-icon" />
              <div className="user-details">
                <span className="user-name">{user?.username || user?.email}</span>
                <span className="user-role">{roleLabels[user?.role] || user?.role}</span>
              </div>
            </div>
            <button onClick={handleLogout} className="logout-btn">
              <FaSignOutAlt /> Salir
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
