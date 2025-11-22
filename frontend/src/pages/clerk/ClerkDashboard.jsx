import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import ticketService from '../../services/ticketService'
import parcelService from '../../services/parcelService'
import baggageService from '../../services/baggageService'
import {
  FaTicketAlt,
  FaBox,
  FaSuitcase,
  FaDollarSign,
  FaChartLine,
} from 'react-icons/fa'
import '../../styles/ClerkDashboard.css'

const ClerkDashboard = () => {
  const [stats, setStats] = useState({
    ticketsToday: 0,
    parcelsToday: 0,
    baggageToday: 0,
    revenueToday: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      // Aquí normalmente harías llamadas a endpoints específicos para obtener estadísticas
      // Por ahora simulamos datos básicos
      const today = new Date().toISOString().split('T')[0]

      setStats({
        ticketsToday: 0,
        parcelsToday: 0,
        baggageToday: 0,
        revenueToday: 0,
      })
    } catch (err) {
      setError('Error al cargar datos del dashboard')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <Loading message="Cargando dashboard..." />
  }

  return (
    <div className="clerk-dashboard">
      <div className="dashboard-header">
        <h1>Dashboard del Empleado</h1>
        <p className="dashboard-subtitle">Resumen de operaciones del día</p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      {/* Estadísticas */}
      <div className="stats-grid">
        <Card className="stat-card stat-tickets">
          <div className="stat-icon">
            <FaTicketAlt />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.ticketsToday}</div>
            <div className="stat-label">Boletos Vendidos Hoy</div>
          </div>
        </Card>

        <Card className="stat-card stat-parcels">
          <div className="stat-icon">
            <FaBox />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.parcelsToday}</div>
            <div className="stat-label">Encomiendas Registradas</div>
          </div>
        </Card>

        <Card className="stat-card stat-baggage">
          <div className="stat-icon">
            <FaSuitcase />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.baggageToday}</div>
            <div className="stat-label">Equipajes Registrados</div>
          </div>
        </Card>

        <Card className="stat-card stat-revenue">
          <div className="stat-icon">
            <FaDollarSign />
          </div>
          <div className="stat-content">
            <div className="stat-value">${stats.revenueToday.toFixed(2)}</div>
            <div className="stat-label">Ingresos del Día</div>
          </div>
        </Card>
      </div>

      {/* Accesos rápidos */}
      <div className="quick-actions-section">
        <h2>Accesos Rápidos</h2>
        <div className="quick-actions-grid">
          <Card
            className="quick-action-card"
            onClick={() => window.location.href = '/clerk/sales'}
            style={{ cursor: 'pointer' }}
          >
            <FaTicketAlt size={32} color="var(--primary-color)" />
            <h3>Venta Rápida</h3>
            <p>Vender boletos para viajes disponibles</p>
          </Card>

          <Card
            className="quick-action-card"
            onClick={() => window.location.href = '/clerk/create-parcel'}
            style={{ cursor: 'pointer' }}
          >
            <FaBox size={32} color="var(--accent-color)" />
            <h3>Registrar Encomienda</h3>
            <p>Crear nueva encomienda para un viaje</p>
          </Card>

          <Card
            className="quick-action-card"
            onClick={() => window.location.href = '/clerk/parcels'}
            style={{ cursor: 'pointer' }}
          >
            <FaSuitcase size={32} color="var(--verdigris)" />
            <h3>Registrar Equipaje</h3>
            <p>Asociar equipaje a un boleto existente</p>
          </Card>

          <Card className="quick-action-card" style={{ opacity: 0.6 }}>
            <FaChartLine size={32} color="var(--mint-leaf)" />
            <h3>Ver Reportes</h3>
            <p>Consultar estadísticas y reportes</p>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default ClerkDashboard
