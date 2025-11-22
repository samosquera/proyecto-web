import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import Button from '../components/Button'
import {
  FaTicketAlt,
  FaSearch,
  FaBus,
  FaUsers,
  FaRoute,
  FaCog,
  FaTruck,
  FaClipboardList,
} from 'react-icons/fa'
import '../styles/Dashboard.css'

const Dashboard = () => {
  const { user, hasRole } = useAuth()
  const navigate = useNavigate()

  const getDashboardCards = () => {
    const cards = []

    // Tarjetas para todos los usuarios autenticados
    cards.push({
      title: 'Buscar Viajes',
      description: 'Encuentra y reserva tu próximo viaje',
      icon: <FaSearch />,
      action: () => navigate('/search-trips'),
      color: 'blue',
    })

    // Tarjetas para pasajeros
    if (hasRole(['PASSENGER', 'CUSTOMER'])) {
      cards.push({
        title: 'Mis Boletos',
        description: 'Ver y gestionar mis boletos',
        icon: <FaTicketAlt />,
        action: () => navigate('/my-tickets'),
        color: 'green',
      })
    }

    // Tarjetas para conductores
    if (hasRole('DRIVER')) {
      cards.push(
        {
          title: 'Mis Viajes',
          description: 'Gestionar viajes asignados',
          icon: <FaBus />,
          action: () => navigate('/driver/trips'),
          color: 'orange',
        },
        {
          title: 'Checklists',
          description: 'Checklists de pre-viaje',
          icon: <FaClipboardList />,
          action: () => navigate('/driver/checklists'),
          color: 'purple',
        }
      )
    }

    // Tarjetas para empleados (clerk)
    if (hasRole(['CLERK', 'ADMIN'])) {
      cards.push(
        {
          title: 'Venta Rápida',
          description: 'Vender boletos rápidamente',
          icon: <FaTicketAlt />,
          action: () => navigate('/clerk/sales'),
          color: 'green',
        },
        {
          title: 'Paquetería',
          description: 'Gestión de paquetes',
          icon: <FaTruck />,
          action: () => navigate('/clerk/parcels'),
          color: 'brown',
        }
      )
    }

    // Tarjetas para despachadores
    if (hasRole(['DISPATCHER', 'ADMIN'])) {
      cards.push(
        {
          title: 'Gestión de Viajes',
          description: 'Crear y gestionar viajes',
          icon: <FaBus />,
          action: () => navigate('/dispatcher/trips'),
          color: 'blue',
        },
        {
          title: 'Asignaciones',
          description: 'Asignar conductores a viajes',
          icon: <FaClipboardList />,
          action: () => navigate('/dispatcher/assignments'),
          color: 'purple',
        }
      )
    }

    // Tarjetas para administradores
    if (hasRole('ADMIN')) {
      cards.push(
        {
          title: 'Usuarios',
          description: 'Gestionar usuarios del sistema',
          icon: <FaUsers />,
          action: () => navigate('/admin/users'),
          color: 'red',
        },
        {
          title: 'Autobuses',
          description: 'Gestionar flota de autobuses',
          icon: <FaBus />,
          action: () => navigate('/admin/buses'),
          color: 'orange',
        },
        {
          title: 'Rutas',
          description: 'Gestionar rutas y paradas',
          icon: <FaRoute />,
          action: () => navigate('/admin/routes'),
          color: 'teal',
        },
        {
          title: 'Configuración',
          description: 'Configuración del sistema',
          icon: <FaCog />,
          action: () => navigate('/admin/config'),
          color: 'gray',
        }
      )
    }

    return cards
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
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Bienvenido, {user?.username || user?.email}</h1>
        <p className="dashboard-subtitle">
          Rol: {roleLabels[user?.role] || user?.role}
        </p>
      </div>

      <div className="dashboard-grid">
        {getDashboardCards().map((card, index) => (
          <Card key={index} className={`dashboard-card card-${card.color}`}>
            <div className="dashboard-card-content">
              <div className="dashboard-card-icon">{card.icon}</div>
              <h3>{card.title}</h3>
              <p>{card.description}</p>
              <Button onClick={card.action} variant="secondary" size="small">
                Acceder
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}

export default Dashboard
