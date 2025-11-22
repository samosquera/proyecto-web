import React, { useState, useEffect } from 'react'
import tripService from '../../services/tripService'
import routeService from '../../services/routeService'
import busService from '../../services/busService'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaPlus, FaEdit, FaSearch, FaBus, FaEye } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const ManageTrips = () => {
  const [trips, setTrips] = useState([])
  const [filteredTrips, setFilteredTrips] = useState([])
  const [routes, setRoutes] = useState([])
  const [buses, setBuses] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showModal, setShowModal] = useState(false)
  const [editingTrip, setEditingTrip] = useState(null)

  const [formData, setFormData] = useState({
    date: '',
    departureAt: '',
    arrivalEta: '',
    routeId: '',
    busId: '',
    status: 'SCHEDULED',
  })

  useEffect(() => {
    loadTrips()
    loadRoutes()
    loadBuses()
  }, [])

  useEffect(() => {
    filterTrips()
  }, [trips, searchTerm, statusFilter])

  const loadTrips = async () => {
    try {
      setLoading(true)
      const data = await tripService.getAllTrips()
      setTrips(data)
    } catch (err) {
      setError('Error al cargar viajes: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const loadRoutes = async () => {
    try {
      const data = await routeService.getAllRoutes()
      setRoutes(data)
    } catch (err) {
      console.error('Error al cargar rutas:', err)
    }
  }

  const loadBuses = async () => {
    try {
      const data = await busService.getAllBuses()
      setBuses(data)
    } catch (err) {
      console.error('Error al cargar buses:', err)
    }
  }

  const filterTrips = () => {
    let filtered = trips

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((t) => t.status === statusFilter)
    }

    if (searchTerm) {
      filtered = filtered.filter(
        (t) =>
          t.route?.origin?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          t.route?.destination?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          t.bus?.plate?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    setFilteredTrips(filtered)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    try {
      setLoading(true)
      // Asegurar formato LocalDateTime con segundos (yyyy-MM-ddTHH:mm:ss)
      const departureDateTime = formData.departureAt.includes(':00', formData.departureAt.length - 3)
        ? formData.departureAt
        : `${formData.departureAt}:00`
      const arrivalDateTime = formData.arrivalEta.includes(':00', formData.arrivalEta.length - 3)
        ? formData.arrivalEta
        : `${formData.arrivalEta}:00`

      if (editingTrip) {
        // Para actualizar, solo enviar departureAt, arrivalEta, busId y status
        const updateData = {
          departureAt: departureDateTime,
          arrivalEta: arrivalDateTime,
          busId: formData.busId ? parseInt(formData.busId) : null,
          status: formData.status,
        }
        await tripService.updateTrip(editingTrip.id, updateData)
        setSuccess('Viaje actualizado exitosamente')
      } else {
        // Para crear, enviar todos los campos
        const createData = {
          date: formData.date,
          departureAt: departureDateTime,
          arrivalEta: arrivalDateTime,
          routeId: parseInt(formData.routeId),
          busId: formData.busId ? parseInt(formData.busId) : null,
        }
        await tripService.createTrip(createData)
        setSuccess('Viaje creado exitosamente')
      }
      setShowModal(false)
      setFormData({
        date: '',
        departureAt: '',
        arrivalEta: '',
        routeId: '',
        busId: '',
        status: 'SCHEDULED',
      })
      setEditingTrip(null)
      loadTrips()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar viaje')
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (trip) => {
    setEditingTrip(trip)
    setFormData({
      date: trip.date || '',
      departureAt: trip.departureAt ? trip.departureAt.slice(0, 16) : '',
      arrivalEta: trip.arrivalEta ? trip.arrivalEta.slice(0, 16) : '',
      routeId: trip.routeId || '',
      busId: trip.busId || '',
      status: trip.status || 'SCHEDULED',
    })
    setShowModal(true)
  }

  const getStatusLabel = (status) => {
    const labels = {
      SCHEDULED: 'Programado',
      BOARDING: 'Abordando',
      DEPARTED: 'Partido',
      ARRIVED: 'Llegado',
      CANCELLED: 'Cancelado',
    }
    return labels[status] || status
  }

  const formatDateTime = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (loading && trips.length === 0) {
    return <Loading message="Cargando viajes..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Gestión de Viajes</h1>
          <p>Administre los viajes programados</p>
        </div>
        <Button onClick={() => setShowModal(true)} icon={<FaPlus />}>
          Nuevo Viaje
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="filters-card">
        <div className="filters-grid">
          <Input
            placeholder="Buscar por ruta o placa de bus..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<FaSearch />}
          />
          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { value: 'ALL', label: 'Todos los estados' },
              { value: 'SCHEDULED', label: 'Programados' },
              { value: 'BOARDING', label: 'Abordando' },
              { value: 'DEPARTED', label: 'Partidos' },
              { value: 'ARRIVED', label: 'Llegados' },
              { value: 'CANCELLED', label: 'Cancelados' },
            ]}
          />
        </div>
      </Card>

      <Card className="table-card">
        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Ruta</th>
                <th>Bus</th>
                <th>Fecha Salida</th>
                <th>Llegada Estimada</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredTrips.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center">
                    No se encontraron viajes
                  </td>
                </tr>
              ) : (
                filteredTrips.map((trip) => (
                  <tr key={trip.id}>
                    <td>{trip.id}</td>
                    <td>
                      {trip.origin} → {trip.destination}
                    </td>
                    <td>
                      <div className="user-cell">
                        <FaBus />
                        <span>{trip.busPlate}</span>
                      </div>
                    </td>
                    <td>{formatDateTime(trip.departureAt)}</td>
                    <td>{formatDateTime(trip.arrivalEta)}</td>
                    <td>
                      <span className={`badge badge-${trip.status?.toLowerCase()}`}>
                        {getStatusLabel(trip.status)}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-icon" onClick={() => handleEdit(trip)} title="Editar">
                          <FaEdit />
                        </button>
                        <button className="btn-icon" title="Ver detalles">
                          <FaEye />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingTrip ? 'Editar Viaje' : 'Nuevo Viaje'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <Input
                  label="Fecha del Viaje"
                  type="date"
                  name="date"
                  value={formData.date}
                  onChange={handleInputChange}
                  required={!editingTrip}
                  disabled={editingTrip}
                />
                <Select
                  label="Ruta"
                  name="routeId"
                  value={formData.routeId}
                  onChange={handleInputChange}
                  options={[
                    { value: '', label: 'Seleccione una ruta' },
                    ...routes.map((r) => ({
                      value: r.id,
                      label: `${r.origin} → ${r.destination}`,
                    })),
                  ]}
                  required={!editingTrip}
                  disabled={editingTrip}
                />
                <Select
                  label="Bus"
                  name="busId"
                  value={formData.busId}
                  onChange={handleInputChange}
                  options={[
                    { value: '', label: 'Seleccione un bus' },
                    ...buses.map((b) => ({
                      value: b.id,
                      label: `${b.plate} (${b.capacity} asientos)`,
                    })),
                  ]}
                />
                <Input
                  label="Fecha y Hora de Salida"
                  type="datetime-local"
                  name="departureAt"
                  value={formData.departureAt}
                  onChange={handleInputChange}
                  required
                />
                <Input
                  label="Fecha y Hora de Llegada Estimada"
                  type="datetime-local"
                  name="arrivalEta"
                  value={formData.arrivalEta}
                  onChange={handleInputChange}
                  required
                />
                {editingTrip && (
                  <Select
                    label="Estado"
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    options={[
                      { value: 'SCHEDULED', label: 'Programado' },
                      { value: 'BOARDING', label: 'Abordando' },
                      { value: 'DEPARTED', label: 'Partido' },
                      { value: 'ARRIVED', label: 'Llegado' },
                      { value: 'CANCELLED', label: 'Cancelado' },
                    ]}
                    required
                  />
                )}
              </div>
              {editingTrip && (
                <Alert
                  type="info"
                  message="Al editar solo se pueden cambiar: Fecha/Hora de Salida, Llegada Estimada, Bus y Estado"
                />
              )}
              <div className="modal-actions">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </Button>
                <Button type="submit" loading={loading}>
                  {editingTrip ? 'Actualizar' : 'Crear'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageTrips
