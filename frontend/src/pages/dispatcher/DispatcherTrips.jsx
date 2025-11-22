import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import tripService from '../../services/tripService'
import routeService from '../../services/routeService'
import busService from '../../services/busService'
import { FaBus, FaPlus, FaEdit, FaTrash } from 'react-icons/fa'

const DispatcherTrips = () => {
  const [trips, setTrips] = useState([])
  const [routes, setRoutes] = useState([])
  const [buses, setBuses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    routeId: '',
    busId: '',
    date: '',
    departureAt: '',
    arrivalEta: '',
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [tripsData, routesData, busesData] = await Promise.all([
        tripService.getTodayTrips(),
        routeService.getAllRoutes(),
        busService.getAvailableBuses(),
      ])
      setTrips(tripsData)
      setRoutes(routesData.map((r) => ({ value: r.id, label: `${r.origin} - ${r.destination}` })))
      setBuses(busesData.map((b) => ({ value: b.id, label: `${b.plate} (${b.capacity} asientos)` })))
    } catch (err) {
      setError('Error al cargar datos')
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
      // Combinar fecha + hora para crear LocalDateTime en formato ISO
      const departureDateTime = `${formData.date}T${formData.departureAt}:00`
      const arrivalDateTime = `${formData.date}T${formData.arrivalEta}:00`

      const tripData = {
        routeId: parseInt(formData.routeId),
        busId: formData.busId ? parseInt(formData.busId) : null,
        date: formData.date,
        departureAt: departureDateTime,
        arrivalEta: arrivalDateTime,
      }

      await tripService.createTrip(tripData)
      setSuccess('Viaje creado exitosamente')
      setShowForm(false)
      setFormData({
        routeId: '',
        busId: '',
        date: '',
        departureAt: '',
        arrivalEta: '',
      })
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear viaje')
    }
  }

  const handleCancelTrip = async (id) => {
    if (!window.confirm('¿Está seguro de cancelar este viaje?')) return

    try {
      await tripService.cancelTrip(id, 'Cancelado por dispatcher')
      setSuccess('Viaje cancelado')
      loadData()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cancelar viaje')
    }
  }

  if (loading) {
    return <Loading message="Cargando viajes..." />
  }

  return (
    <div className="dispatcher-trips">
      <div className="page-header">
        <h1>Gestión de Viajes</h1>
        <Button onClick={() => setShowForm(!showForm)} icon={<FaPlus />}>
          {showForm ? 'Cancelar' : 'Crear Viaje'}
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {showForm && (
        <Card className="form-card">
          <h3>Crear Nuevo Viaje</h3>
          <form onSubmit={handleSubmit} className="trip-form">
            <Select
              label="Ruta"
              name="routeId"
              value={formData.routeId}
              onChange={handleInputChange}
              options={routes}
              required
            />
            <Select
              label="Autobús"
              name="busId"
              value={formData.busId}
              onChange={handleInputChange}
              options={buses}
              required
            />
            <Input
              label="Fecha"
              type="date"
              name="date"
              value={formData.date}
              onChange={handleInputChange}
              required
            />
            <Input
              label="Hora de Salida"
              type="time"
              name="departureAt"
              value={formData.departureAt}
              onChange={handleInputChange}
              required
            />
            <Input
              label="Hora Estimada de Llegada"
              type="time"
              name="arrivalEta"
              value={formData.arrivalEta}
              onChange={handleInputChange}
              required
            />
            <Button type="submit" fullWidth>
              Crear Viaje
            </Button>
          </form>
        </Card>
      )}

      <div className="trips-list">
        <h2>Viajes de Hoy</h2>
        {trips.length === 0 ? (
          <Card>
            <p>No hay viajes programados para hoy</p>
          </Card>
        ) : (
          <div className="trips-grid">
            {trips.map((trip) => (
              <Card key={trip.id} className="trip-item">
                <div className="trip-info">
                  <h3>
                    {trip.origin} → {trip.destination}
                  </h3>
                  <p>Bus: {trip.busPlate}</p>
                  <p>Salida: {trip.departureAt}</p>
                  <p>Estado: {trip.status}</p>
                </div>
                <div className="trip-actions">
                  <Button variant="danger" size="small" onClick={() => handleCancelTrip(trip.id)}>
                    <FaTrash /> Cancelar
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default DispatcherTrips
