import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import assignmentService from '../../services/assignmentService'
import tripService from '../../services/tripService'
import userService from '../../services/userService'
import { FaUserTie, FaBus, FaCheckCircle, FaPlus } from 'react-icons/fa'
import { format } from 'date-fns'

const DispatcherAssignments = () => {
  const [assignments, setAssignments] = useState([])
  const [unassignedTrips, setUnassignedTrips] = useState([])
  const [drivers, setDrivers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    tripId: '',
    driverId: '',
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [assignmentsData, tripsData, driversData] = await Promise.all([
        assignmentService.getAllAssignments(),
        tripService.getTodayTrips(),
        userService.getUsersByRoleAndStatus('DRIVER', 'ACTIVE'),
      ])

      setAssignments(assignmentsData)

      // Filtrar viajes sin asignar
      const tripIds = new Set(assignmentsData.map((a) => a.tripId))
      const unassigned = tripsData.filter(
        (t) => !tripIds.has(t.id) && t.status !== 'CANCELLED' && t.status !== 'ARRIVED'
      )
      setUnassignedTrips(unassigned)

      setDrivers(
        driversData.map((d) => ({
          value: d.id,
          label: `${d.username} - ${d.email}`,
        }))
      )
    } catch (err) {
      setError('Error al cargar datos: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await assignmentService.createAssignment({
        ...formData,
        dispatcherId: null, // El backend obtendrá el ID del dispatcher del token
      })
      setSuccess('Asignación creada exitosamente')
      setShowForm(false)
      setFormData({ tripId: '', driverId: '' })
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear asignación')
    }
  }

  const handleApproveChecklist = async (assignmentId) => {
    try {
      await assignmentService.approveChecklist(assignmentId)
      setSuccess('Checklist aprobado exitosamente')
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al aprobar checklist')
    }
  }

  const getTripLabel = (trip) => {
    return `${trip.origin} → ${trip.destination} - ${trip.departureAt}`
  }

  const getStatusBadge = (status) => {
    const badges = {
      SCHEDULED: { color: 'blue', label: 'Programado' },
      BOARDING: { color: 'yellow', label: 'Abordando' },
      DEPARTED: { color: 'green', label: 'En ruta' },
      ARRIVED: { color: 'gray', label: 'Llegado' },
      CANCELLED: { color: 'red', label: 'Cancelado' },
    }
    const badge = badges[status] || { color: 'gray', label: status }
    return (
      <span
        style={{
          backgroundColor: badge.color,
          color: 'white',
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '0.85rem',
        }}
      >
        {badge.label}
      </span>
    )
  }

  if (loading) {
    return <Loading message="Cargando asignaciones..." />
  }

  return (
    <div className="dispatcher-assignments">
      <div className="page-header">
        <h1>Asignaciones de Conductores</h1>
        <Button onClick={() => setShowForm(!showForm)} icon={<FaPlus />}>
          {showForm ? 'Cancelar' : 'Nueva Asignación'}
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {showForm && (
        <Card className="form-card">
          <h3>Crear Nueva Asignación</h3>
          {unassignedTrips.length === 0 ? (
            <Alert type="info" message="No hay viajes sin asignar" />
          ) : (
            <form onSubmit={handleSubmit} className="assignment-form">
              <Select
                label="Viaje"
                name="tripId"
                value={formData.tripId}
                onChange={handleInputChange}
                options={unassignedTrips.map((t) => ({
                  value: t.id,
                  label: getTripLabel(t),
                }))}
                placeholder="Seleccione un viaje"
                required
              />
              <Select
                label="Conductor"
                name="driverId"
                value={formData.driverId}
                onChange={handleInputChange}
                options={drivers}
                placeholder="Seleccione un conductor"
                required
              />
              <Button type="submit" fullWidth>
                Crear Asignación
              </Button>
            </form>
          )}
        </Card>
      )}

      <div className="assignments-section">
        <h2>Asignaciones Activas ({assignments.length})</h2>
        {assignments.length === 0 ? (
          <Card>
            <p>No hay asignaciones registradas</p>
          </Card>
        ) : (
          <div className="assignments-grid">
            {assignments.map((assignment) => (
              <Card key={assignment.id} className="assignment-card">
                <div className="assignment-header">
                  <div className="assignment-info">
                    <h3>
                      <FaBus style={{ marginRight: '8px' }} />
                      {assignment.tripInfo || 'Viaje sin información'}
                    </h3>
                    <p style={{ margin: '4px 0', color: '#666' }}>
                      {assignment.tripDate} - {assignment.tripDepartureTime}
                    </p>
                  </div>
                  {getStatusBadge(assignment.tripStatus)}
                </div>

                <div className="assignment-body">
                  <div className="info-row">
                    <FaUserTie />
                    <span>
                      <strong>Conductor:</strong> {assignment.driverName || 'Sin nombre'}
                    </span>
                  </div>
                  <div className="info-row">
                    <span>
                      <strong>Ruta:</strong> {assignment.routeInfo || 'Sin información'}
                    </span>
                  </div>
                  <div className="info-row">
                    <span>
                      <strong>Bus:</strong> Placa N/A
                    </span>
                  </div>
                  <div className="info-row">
                    <span>
                      <strong>Dispatcher:</strong> {assignment.dispatcherName || 'Sin asignar'}
                    </span>
                  </div>
                  <div className="info-row">
                    <span>
                      <strong>Checklist:</strong>{' '}
                      {assignment.checklistOk ? (
                        <span style={{ color: 'green' }}>
                          <FaCheckCircle /> Aprobado
                        </span>
                      ) : (
                        <span style={{ color: 'orange' }}>Pendiente</span>
                      )}
                    </span>
                  </div>
                  <div className="info-row">
                    <span style={{ fontSize: '0.85rem', color: '#666' }}>
                      Asignado: {assignment.assignedAt}
                    </span>
                  </div>
                </div>

                {!assignment.checklistOk &&
                  assignment.tripStatus === 'SCHEDULED' && (
                    <div className="assignment-actions">
                      <Button
                        onClick={() => handleApproveChecklist(assignment.id)}
                        variant="success"
                        size="small"
                        icon={<FaCheckCircle />}
                      >
                        Aprobar Checklist
                      </Button>
                    </div>
                  )}
              </Card>
            ))}
          </div>
        )}
      </div>

      {unassignedTrips.length > 0 && (
        <div className="unassigned-section" style={{ marginTop: '32px' }}>
          <h2>Viajes sin Asignar ({unassignedTrips.length})</h2>
          <div className="trips-grid">
            {unassignedTrips.map((trip) => (
              <Card key={trip.id} className="trip-card">
                <div className="trip-info">
                  <h3>{getTripLabel(trip)}</h3>
                  <p>Bus: {trip.busPlate || 'Sin bus'}</p>
                  <p>Fecha: {trip.date}</p>
                  {getStatusBadge(trip.status)}
                </div>
                <Button
                  onClick={() => {
                    setFormData((prev) => ({ ...prev, tripId: trip.id }))
                    setShowForm(true)
                  }}
                  size="small"
                  variant="primary"
                >
                  Asignar Conductor
                </Button>
              </Card>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default DispatcherAssignments
