import React, { useState, useEffect } from 'react'
import busService from '../../services/busService'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaPlus, FaEdit, FaTrash, FaSearch, FaBus } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const ManageBuses = () => {
  const [buses, setBuses] = useState([])
  const [filteredBuses, setFilteredBuses] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showModal, setShowModal] = useState(false)
  const [editingBus, setEditingBus] = useState(null)

  const [formData, setFormData] = useState({
    plate: '',
    capacity: '',
    amenities: {},
    status: 'ACTIVE',
  })

  useEffect(() => {
    loadBuses()
  }, [])

  useEffect(() => {
    filterBuses()
  }, [buses, searchTerm, statusFilter])

  const loadBuses = async () => {
    try {
      setLoading(true)
      const data = await busService.getAllBuses()
      setBuses(data)
    } catch (err) {
      setError('Error al cargar autobuses: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const filterBuses = () => {
    let filtered = buses

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((b) => b.status === statusFilter)
    }

    if (searchTerm) {
      filtered = filtered.filter((b) =>
        b.plate?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    setFilteredBuses(filtered)
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
      if (editingBus) {
        // Para actualizar, solo enviar capacity, amenities y status (NO plate)
        const updateData = {
          capacity: parseInt(formData.capacity),
          amenities: formData.amenities || {},
          status: formData.status,
        }
        await busService.updateBus(editingBus.id, updateData)
        setSuccess('Autobús actualizado exitosamente')
      } else {
        // Para crear, enviar todos los campos
        const createData = {
          plate: formData.plate,
          capacity: parseInt(formData.capacity),
          amenities: formData.amenities || {},
          status: formData.status,
        }
        await busService.createBus(createData)
        setSuccess('Autobús creado exitosamente')
      }
      setShowModal(false)
      setFormData({
        plate: '',
        capacity: '',
        amenities: {},
        status: 'ACTIVE',
      })
      setEditingBus(null)
      loadBuses()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar autobús')
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (bus) => {
    setEditingBus(bus)
    setFormData({
      plate: bus.plate || '',
      capacity: bus.capacity || '',
      amenities: bus.amenities || {},
      status: bus.status || 'ACTIVE',
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar este autobús?')) return

    try {
      setLoading(true)
      await busService.deleteBus(id)
      setSuccess('Autobús eliminado exitosamente')
      loadBuses()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al eliminar autobús')
    } finally {
      setLoading(false)
    }
  }

  const handleChangeStatus = async (id, currentStatus) => {
    const statusOptions = ['ACTIVE', 'MAINTENANCE', 'INACTIVE']
    const currentIndex = statusOptions.indexOf(currentStatus)
    const newStatus = statusOptions[(currentIndex + 1) % statusOptions.length]

    try {
      setLoading(true)
      await busService.changeBusStatus(id, newStatus)
      setSuccess('Estado actualizado exitosamente')
      loadBuses()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cambiar estado')
    } finally {
      setLoading(false)
    }
  }

  const getStatusLabel = (status) => {
    const labels = {
      ACTIVE: 'Disponible',
      MAINTENANCE: 'Mantenimiento',
      INACTIVE: 'Fuera de Servicio',
    }
    return labels[status] || status
  }

  if (loading && buses.length === 0) {
    return <Loading message="Cargando autobuses..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Gestión de Autobuses</h1>
          <p>Administre la flota de autobuses</p>
        </div>
        <Button onClick={() => setShowModal(true)} icon={<FaPlus />}>
          Nuevo Autobús
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="filters-card">
        <div className="filters-grid">
          <Input
            placeholder="Buscar por placa..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<FaSearch />}
          />
          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { value: 'ALL', label: 'Todos los estados' },
              { value: 'ACTIVE', label: 'Disponibles' },
              { value: 'MAINTENANCE', label: 'En Mantenimiento' },
              { value: 'INACTIVE', label: 'Fuera de Servicio' },
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
                <th>Placa</th>
                <th>Capacidad</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredBuses.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center">
                    No se encontraron autobuses
                  </td>
                </tr>
              ) : (
                filteredBuses.map((bus) => (
                  <tr key={bus.id}>
                    <td>{bus.id}</td>
                    <td>
                      <div className="user-cell">
                        <FaBus />
                        <span>{bus.plate}</span>
                      </div>
                    </td>
                    <td>{bus.capacity} asientos</td>
                    <td>
                      <button
                        className={`status-badge ${bus.status?.toLowerCase()}`}
                        onClick={() => handleChangeStatus(bus.id, bus.status)}
                      >
                        {getStatusLabel(bus.status)}
                      </button>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-icon" onClick={() => handleEdit(bus)} title="Editar">
                          <FaEdit />
                        </button>
                        <button
                          className="btn-icon btn-danger"
                          onClick={() => handleDelete(bus.id)}
                          title="Eliminar"
                        >
                          <FaTrash />
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
            <h2>{editingBus ? 'Editar Autobús' : 'Nuevo Autobús'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <Input
                  label="Placa"
                  name="plate"
                  value={formData.plate}
                  onChange={handleInputChange}
                  required={!editingBus}
                  disabled={editingBus}
                  placeholder="ABC-1234"
                  maxLength="20"
                />
                <Input
                  label="Capacidad"
                  type="number"
                  name="capacity"
                  value={formData.capacity}
                  onChange={handleInputChange}
                  required
                  min="1"
                  placeholder="40"
                />
                <Select
                  label="Estado"
                  name="status"
                  value={formData.status}
                  onChange={handleInputChange}
                  options={[
                    { value: 'ACTIVE', label: 'Disponible' },
                    { value: 'MAINTENANCE', label: 'Mantenimiento' },
                    { value: 'INACTIVE', label: 'Fuera de Servicio' },
                  ]}
                  required
                />
              </div>
              {editingBus && (
                <Alert
                  type="info"
                  message="Al editar solo se pueden cambiar: Capacidad y Estado. La placa no es editable."
                />
              )}
              <div className="modal-actions">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </Button>
                <Button type="submit" loading={loading}>
                  {editingBus ? 'Actualizar' : 'Crear'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageBuses
