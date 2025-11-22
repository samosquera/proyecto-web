import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import {
  FaBus,
  FaBox,
  FaInfoCircle,
  FaSignInAlt,
  FaUserPlus,
  FaUser,
  FaSignOutAlt,
  FaChevronLeft,
  FaChevronRight,
  FaTachometerAlt,
  FaUsers,
  FaTruck,
  FaClipboardList,
  FaRoute,
  FaTicketAlt,
  FaCog
} from 'react-icons/fa'
import '../styles/Sidebar.css'

const Sidebar = ({ onOpenLogin, onOpenRegister }) => {
  const { user, logout, isAuthenticated } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(true)

  const handleLogout = async () => {
    await logout()
    navigate('/')
  }

  const toggleSidebar = () => {
    setCollapsed(!collapsed)
  }

  // Navegación para usuarios no autenticados
  const publicLinks = [
    { to: '/', icon: <FaBus />, label: 'Viajes' },
    { to: '/parcels', icon: <FaBox />, label: 'Encomiendas' },
    { to: '/about', icon: <FaInfoCircle />, label: 'About Bers App' },
  ]

  // Navegación para pasajeros/clientes
  const passengerLinks = [
    { to: '/', icon: <FaBus />, label: 'Buscar Viajes' },
    { to: '/my-tickets', icon: <FaTicketAlt />, label: 'Mis Boletos' },
    { to: '/parcels', icon: <FaBox />, label: 'Encomiendas' },
    { to: '/track-parcel', icon: <FaRoute />, label: 'Rastrear Encomienda' },
    { to: '/about', icon: <FaInfoCircle />, label: 'About Bers App' },
  ]

  // Navegación para administradores
  const adminLinks = [
    { to: '/admin/dashboard', icon: <FaTachometerAlt />, label: 'Dashboard' },
    { to: '/admin/users', icon: <FaUsers />, label: 'Usuarios' },
    { to: '/admin/buses', icon: <FaBus />, label: 'Buses' },
    { to: '/admin/routes', icon: <FaRoute />, label: 'Rutas' },
    { to: '/admin/trips', icon: <FaClipboardList />, label: 'Viajes' },
    { to: '/admin/parcels', icon: <FaBox />, label: 'Encomiendas' },
    { to: '/admin/settings', icon: <FaCog />, label: 'Configuración' },
  ]

  // Navegación para dispatchers
  const dispatcherLinks = [
    { to: '/dispatcher/dashboard', icon: <FaTachometerAlt />, label: 'Dashboard' },
    { to: '/dispatcher/trips', icon: <FaClipboardList />, label: 'Gestión de Viajes' },
    { to: '/dispatcher/assignments', icon: <FaTruck />, label: 'Asignaciones' },
    { to: '/dispatcher/dispatch', icon: <FaRoute />, label: 'Despacho' },
    { to: '/dispatcher/overbooking', icon: <FaUsers />, label: 'Overbooking' },
  ]

  // Navegación para conductores
  const driverLinks = [
    { to: '/driver/dashboard', icon: <FaTachometerAlt />, label: 'Dashboard' },
    { to: '/driver/trips', icon: <FaBus />, label: 'Mis Viajes' },
    { to: '/driver/parcels', icon: <FaBox />, label: 'Encomiendas' },
    { to: '/driver/deliver', icon: <FaTruck />, label: 'Entregar Encomienda' },
  ]

  // Navegación para empleados (clerks)
  const clerkLinks = [
    { to: '/clerk/dashboard', icon: <FaTachometerAlt />, label: 'Dashboard' },
    { to: '/clerk/sales', icon: <FaTicketAlt />, label: 'Venta de Boletos' },
    { to: '/clerk/parcels', icon: <FaBox />, label: 'Registro Encomiendas' },
    { to: '/clerk/create-parcel', icon: <FaBox />, label: 'Nueva Encomienda' },
  ]

  // Determinar qué enlaces mostrar según el rol
  const getNavigationLinks = () => {
    if (!isAuthenticated()) {
      return publicLinks
    }

    switch (user?.role) {
      case 'ADMIN':
        return adminLinks
      case 'DISPATCHER':
        return dispatcherLinks
      case 'DRIVER':
        return driverLinks
      case 'CLERK':
        return clerkLinks
      case 'PASSENGER':
      case 'CUSTOMER':
        return passengerLinks
      default:
        return publicLinks
    }
  }

  const navigationLinks = getNavigationLinks()

  const roleLabels = {
    ADMIN: 'Administrador',
    DISPATCHER: 'Despachador',
    DRIVER: 'Conductor',
    CLERK: 'Empleado',
    PASSENGER: 'Pasajero',
    CUSTOMER: 'Cliente',
  }

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      {/* Header con logo */}
      <div className="sidebar-header">
        <div className="sidebar-logo">
          <FaBus className="logo-icon" />
          {!collapsed && <span className="logo-text">BERS App</span>}
        </div>
        <button className="sidebar-toggle" onClick={toggleSidebar}>
          {collapsed ? <FaChevronRight /> : <FaChevronLeft />}
        </button>
      </div>

      {/* Navegación principal */}
      <nav className="sidebar-nav">
        <ul className="nav-links">
          {navigationLinks.map((link) => (
            <li key={link.to}>
              <Link
                to={link.to}
                className={`nav-link ${location.pathname === link.to ? 'active' : ''}`}
                title={collapsed ? link.label : ''}
              >
                <span className="nav-icon">{link.icon}</span>
                {!collapsed && <span className="nav-label">{link.label}</span>}
              </Link>
            </li>
          ))}
        </ul>
      </nav>

      {/* Footer con usuario o botones de login/registro */}
      <div className="sidebar-footer">
        {isAuthenticated() ? (
          <>
            <div className="user-info">
              <div className="user-avatar">
                <FaUser />
              </div>
              {!collapsed && (
                <div className="user-details">
                  <span className="user-name">{user?.username || user?.email}</span>
                  <span className="user-role">{roleLabels[user?.role] || user?.role}</span>
                </div>
              )}
            </div>
            <button
              className="logout-btn"
              onClick={handleLogout}
              title={collapsed ? 'Cerrar Sesión' : ''}
            >
              <FaSignOutAlt />
              {!collapsed && <span>Cerrar Sesión</span>}
            </button>
          </>
        ) : (
          <div className="auth-buttons">
            <button
              className="auth-btn login-btn"
              onClick={onOpenLogin}
              title={collapsed ? 'Iniciar Sesión' : ''}
            >
              <FaSignInAlt />
              {!collapsed && <span>Iniciar Sesión</span>}
            </button>
            <button
              className="auth-btn register-btn"
              onClick={onOpenRegister}
              title={collapsed ? 'Registrarse' : ''}
            >
              <FaUserPlus />
              {!collapsed && <span>Registrarse</span>}
            </button>
          </div>
        )}
      </div>
    </aside>
  )
}

export default Sidebar
