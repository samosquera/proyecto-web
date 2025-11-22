import React, { useState, useEffect } from 'react'
import Card from '../components/Card'
import Button from '../components/Button'
import Loading from '../components/Loading'
import Alert from '../components/Alert'
import tripService from '../services/tripService'
import { FaBus, FaMapMarkerAlt, FaClock, FaCheckCircle } from 'react-icons/fa'
import '../styles/DriverPanel.css'

const DriverPanelOld = () => {
  const [trips, setTrips] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadMyTrips()
  }, [])

  const loadMyTrips = async () => {
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
    } catch (err) {
      setError('Error al cargar viajes asignados')
    } finally {
      setLoading(false)
    }
  }

  const handleStartBoarding = async (tripId) => {
    try {
      setLoading(true)
      await tripService.openBoarding(tripId)
      setSuccess('Abordaje iniciado')
      loadMyTrips()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al iniciar abordaje')
    } finally {
      setLoading(false)
    }
  }

  const handleDepart = async (tripId) => {
    try {
      setLoading(true)
      await tripService.departTrip(tripId)
      setSuccess('Viaje marcado como partido')
      loadMyTrips()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar viaje como partido')
    } finally {
      setLoading(false)
    }
  }

  const handleArrive = async (tripId) => {
    try {
      setLoading(true)
      await tripService.arriveTrip(tripId)
      setSuccess('Viaje marcado como llegado')
      loadMyTrips()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar viaje como llegado')
    } finally {
      setLoading(false)
    }
  }

  const getStatusLabel = (status) => {
    const labels = {
      SCHEDULED: 'Programado',
      BOARDING: 'Abordando',
      DEPARTED: 'En ruta',
      ARRIVED: 'Llegado',
      CANCELLED: 'Cancelado',
    }
    return labels[status] || status
  }

  if (loading) {
    return <Loading message="Cargando viajes..." />
  }

  return (
    <div className="driver-panel">
      <h1>Mis Viajes Asignados</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {trips.length === 0 ? (
        <Card>
          <div className="no-trips">
            <FaBus size={64} />
            <p>No tienes viajes asignados actualmente</p>
          </div>
        </Card>
      ) : (
        <div className="trips-grid">
          {trips.map((assignment) => (
            <Card key={assignment.id} className="driver-trip-card">
              <div className="trip-header">
                <FaBus className="trip-icon" />
                <span className={`trip-status status-${assignment.tripStatus?.toLowerCase() || 'scheduled'}`}>
                  {getStatusLabel(assignment.tripStatus)}
                </span>
              </div>

              <div className="trip-details">
                <div className="detail-row">
                  <FaMapMarkerAlt />
                  <span>
                    {assignment.origin && assignment.destination
                      ? `${assignment.origin} → ${assignment.destination}`
                      : assignment.routeInfo || 'Ruta no especificada'
                    }
                  </span>
                </div>
                <div className="detail-row">
                  <FaClock />
                  <span>
                    Fecha: {assignment.tripDate || 'N/A'} - Salida: {assignment.tripDepartureTime ? new Date(assignment.tripDepartureTime).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' }) : 'N/A'}
                  </span>
                </div>
                <div className="detail-row">
                  <FaBus />
                  <span>Bus: {assignment.busPlate || 'Sin asignar'}</span>
                </div>
                {assignment.capacity && (
                  <div className="detail-row">
                    <FaCheckCircle />
                    <span>Capacidad: {assignment.capacity} asientos</span>
                  </div>
                )}
              </div>

              <div className="trip-actions">
                {assignment.tripStatus === 'SCHEDULED' && (
                  <Button onClick={() => handleStartBoarding(assignment.tripId)} variant="primary">
                    Iniciar Abordaje
                  </Button>
                )}

                {assignment.tripStatus === 'BOARDING' && (
                  <Button onClick={() => handleDepart(assignment.tripId)} variant="success">
                    <FaCheckCircle /> Partir
                  </Button>
                )}

                {assignment.tripStatus === 'DEPARTED' && (
                  <Button onClick={() => handleArrive(assignment.tripId)} variant="success">
                    <FaCheckCircle /> Marcar como Llegado
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

export default DriverPanelOld
