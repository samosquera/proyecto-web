import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import tripService from '../../services/tripService'
import {
  FaBus,
  FaMapMarkerAlt,
  FaClock,
  FaCheckCircle,
  FaBox,
  FaRoute,
} from 'react-icons/fa'
import '../../styles/DriverDashboard.css'

const DriverDashboard = () => {
  const [trips, setTrips] = useState([])
  const [stats, setStats] = useState({
    scheduled: 0,
    boarding: 0,
    departed: 0,
    completed: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      const assignments = await tripService.getActiveAssignedTrips()

      // Enriquecer cada assignment con datos del viaje
      const enrichedTrips = await Promise.all(
        assignments.map(async (assignment) => {
          try {
            // Obtener detalles completos del viaje
            const tripDetails = await tripService.getTripById(assignment.tripId)
            return {
              ...assignment,
              // Agregar datos enriquecidos del TripResponse
              busPlate: tripDetails.busPlate,
              capacity: tripDetails.capacity,
              origin: tripDetails.origin,
              destination: tripDetails.destination,
              routeName: tripDetails.routeName,
            }
          } catch (err) {
            console.warn(`No se pudo enriquecer el viaje ${assignment.tripId}:`, err)
            // Retornar assignment original si falla
            return assignment
          }
        })
      )

      setTrips(enrichedTrips)

      // Calcular estadísticas (usando tripStatus de AssignmentResponse)
      const newStats = {
        scheduled: enrichedTrips.filter((t) => t.tripStatus === 'SCHEDULED').length,
        boarding: enrichedTrips.filter((t) => t.tripStatus === 'BOARDING').length,
        departed: enrichedTrips.filter((t) => t.tripStatus === 'DEPARTED').length,
        completed: enrichedTrips.filter((t) => t.tripStatus === 'ARRIVED').length,
      }
      setStats(newStats)
    } catch (err) {
      setError('Error al cargar datos del dashboard')
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status) => {
    const badges = {
      SCHEDULED: { color: '#028090', label: 'Programado' },
      BOARDING: { color: '#f39c12', label: 'Abordando' },
      DEPARTED: { color: '#02c39a', label: 'En ruta' },
      ARRIVED: { color: '#95a5a6', label: 'Llegado' },
      CANCELLED: { color: '#e74c3c', label: 'Cancelado' },
    }
    const badge = badges[status] || { color: '#95a5a6', label: status }
    return (
      <span
        style={{
          backgroundColor: badge.color,
          color: 'white',
          padding: '4px 12px',
          borderRadius: '6px',
          fontSize: '0.85rem',
          fontWeight: 'bold',
        }}
      >
        {badge.label}
      </span>
    )
  }

  if (loading) {
    return <Loading message="Cargando dashboard..." />
  }

  return (
    <div className="driver-dashboard">
      <div className="dashboard-header">
        <h1>Dashboard del Conductor</h1>
        <p className="dashboard-subtitle">Resumen de tus viajes y actividades</p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      {/* Estadísticas */}
      <div className="stats-grid">
        <Card className="stat-card stat-scheduled">
          <div className="stat-icon">
            <FaClock />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.scheduled}</div>
            <div className="stat-label">Programados</div>
          </div>
        </Card>

        <Card className="stat-card stat-boarding">
          <div className="stat-icon">
            <FaBus />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.boarding}</div>
            <div className="stat-label">Abordando</div>
          </div>
        </Card>

        <Card className="stat-card stat-departed">
          <div className="stat-icon">
            <FaRoute />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.departed}</div>
            <div className="stat-label">En Ruta</div>
          </div>
        </Card>

        <Card className="stat-card stat-completed">
          <div className="stat-icon">
            <FaCheckCircle />
          </div>
          <div className="stat-content">
            <div className="stat-value">{stats.completed}</div>
            <div className="stat-label">Completados</div>
          </div>
        </Card>
      </div>

      {/* Próximos viajes */}
      <div className="upcoming-trips-section">
        <h2>Tus Próximos Viajes</h2>
        {trips.length === 0 ? (
          <Card>
            <div className="no-trips">
              <FaBus size={64} color="var(--text-secondary)" />
              <p>No tienes viajes asignados actualmente</p>
            </div>
          </Card>
        ) : (
          <div className="trips-list">
            {trips.slice(0, 5).map((assignment) => (
              <Card key={assignment.id} className="trip-card">
                <div className="trip-card-header">
                  <div className="trip-route">
                    <FaMapMarkerAlt className="route-icon" />
                    <span className="route-text">
                      {assignment.origin && assignment.destination
                        ? `${assignment.origin} → ${assignment.destination}`
                        : assignment.routeInfo || 'Ruta no especificada'
                      }
                    </span>
                  </div>
                  {getStatusBadge(assignment.tripStatus)}
                </div>

                <div className="trip-card-body">
                  <div className="trip-detail">
                    <FaClock className="detail-icon" />
                    <div className="detail-text">
                      <span className="detail-label">Salida:</span>
                      <span className="detail-value">
                        {assignment.tripDate || 'N/A'} - {assignment.tripDepartureTime ? new Date(assignment.tripDepartureTime).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' }) : 'N/A'}
                      </span>
                    </div>
                  </div>

                  <div className="trip-detail">
                    <FaBus className="detail-icon" />
                    <div className="detail-text">
                      <span className="detail-label">Bus:</span>
                      <span className="detail-value">{assignment.busPlate || 'Sin asignar'}</span>
                    </div>
                  </div>

                  {assignment.capacity && (
                    <div className="trip-detail">
                      <FaBox className="detail-icon" />
                      <div className="detail-text">
                        <span className="detail-label">Capacidad:</span>
                        <span className="detail-value">{assignment.capacity} asientos</span>
                      </div>
                    </div>
                  )}
                </div>
              </Card>
            ))}
          </div>
        )}

        {trips.length > 5 && (
          <p className="more-trips-notice">
            Y {trips.length - 5} viaje{trips.length - 5 !== 1 ? 's' : ''} más...
          </p>
        )}
      </div>
    </div>
  )
}

export default DriverDashboard
