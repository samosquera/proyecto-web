import React, { useState, useEffect } from 'react'
import routeService from '../../services/routeService'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaPlus, FaEdit, FaTrash, FaSearch, FaRoute } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const ManageRoutes = () => {
  const [routes, setRoutes] = useState([])
  const [filteredRoutes, setFilteredRoutes] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editingRoute, setEditingRoute] = useState(null)

  const [formData, setFormData] = useState({
    code: '',
    name: '',
    origin: '',
    destination: '',
    distanceKm: '',
    durationMin: '',
  })

  useEffect(() => {
    loadRoutes()
  }, [])

  useEffect(() => {
    filterRoutes()
  }, [routes, searchTerm])

  const loadRoutes = async () => {
    try {
      setLoading(true)
      const data = await routeService.getAllRoutes()
      setRoutes(data)
    } catch (err) {
      setError('Error al cargar rutas: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const filterRoutes = () => {
    let filtered = routes

    if (searchTerm) {
      filtered = filtered.filter(
        (r) =>
          r.origin?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          r.destination?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          r.code?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    setFilteredRoutes(filtered)
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
      if (editingRoute) {
        // Para actualizar, solo enviar name, distanceKm y durationMin
        const updateData = {
          name: formData.name,
          distanceKm: parseInt(formData.distanceKm),
          durationMin: parseInt(formData.durationMin),
        }
        await routeService.updateRoute(editingRoute.id, updateData)
        setSuccess('Ruta actualizada exitosamente')
      } else {
        // Para crear, enviar todos los campos
        const createData = {
          code: formData.code,
          name: formData.name,
          origin: formData.origin,
          destination: formData.destination,
          distanceKm: parseInt(formData.distanceKm),
          durationMin: parseInt(formData.durationMin),
        }
        await routeService.createRoute(createData)
        setSuccess('Ruta creada exitosamente')
      }
      setShowModal(false)
      setFormData({
        code: '',
        name: '',
        origin: '',
        destination: '',
        distanceKm: '',
        durationMin: '',
      })
      setEditingRoute(null)
      loadRoutes()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar ruta')
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (route) => {
    setEditingRoute(route)
    setFormData({
      code: route.code || '',
      name: route.name || '',
      origin: route.origin || '',
      destination: route.destination || '',
      distanceKm: route.distanceKm || '',
      durationMin: route.durationMin || '',
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar esta ruta?')) return

    try {
      setLoading(true)
      await routeService.deleteRoute(id)
      setSuccess('Ruta eliminada exitosamente')
      loadRoutes()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al eliminar ruta')
    } finally {
      setLoading(false)
    }
  }

  if (loading && routes.length === 0) {
    return <Loading message="Cargando rutas..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Gestión de Rutas</h1>
          <p>Administre las rutas disponibles</p>
        </div>
        <Button onClick={() => setShowModal(true)} icon={<FaPlus />}>
          Nueva Ruta
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="filters-card">
        <div className="filters-grid">
          <Input
            placeholder="Buscar por origen o destino..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<FaSearch />}
          />
        </div>
      </Card>

      <Card className="table-card">
        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Código</th>
                <th>Nombre</th>
                <th>Origen</th>
                <th>Destino</th>
                <th>Distancia (km)</th>
                <th>Duración (min)</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredRoutes.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center">
                    No se encontraron rutas
                  </td>
                </tr>
              ) : (
                filteredRoutes.map((route) => (
                  <tr key={route.id}>
                    <td>{route.id}</td>
                    <td>{route.code}</td>
                    <td>
                      <div className="user-cell">
                        <FaRoute />
                        <span>{route.name}</span>
                      </div>
                    </td>
                    <td>{route.origin}</td>
                    <td>{route.destination}</td>
                    <td>{route.distanceKm} km</td>
                    <td>{route.durationMin} min</td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-icon" onClick={() => handleEdit(route)} title="Editar">
                          <FaEdit />
                        </button>
                        <button
                          className="btn-icon btn-danger"
                          onClick={() => handleDelete(route.id)}
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
            <h2>{editingRoute ? 'Editar Ruta' : 'Nueva Ruta'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <Input
                  label="Código"
                  name="code"
                  value={formData.code}
                  onChange={handleInputChange}
                  required={!editingRoute}
                  disabled={editingRoute}
                  placeholder="RUT-001"
                  maxLength="20"
                />
                <Input
                  label="Nombre"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                  placeholder="Ruta Express"
                />
                <Input
                  label="Origen"
                  name="origin"
                  value={formData.origin}
                  onChange={handleInputChange}
                  required={!editingRoute}
                  disabled={editingRoute}
                  placeholder="Ciudad de origen"
                />
                <Input
                  label="Destino"
                  name="destination"
                  value={formData.destination}
                  onChange={handleInputChange}
                  required={!editingRoute}
                  disabled={editingRoute}
                  placeholder="Ciudad de destino"
                />
                <Input
                  label="Distancia (km)"
                  type="number"
                  name="distanceKm"
                  value={formData.distanceKm}
                  onChange={handleInputChange}
                  required
                  min="1"
                  placeholder="150"
                />
                <Input
                  label="Duración (minutos)"
                  type="number"
                  name="durationMin"
                  value={formData.durationMin}
                  onChange={handleInputChange}
                  required
                  min="1"
                  placeholder="180"
                />
              </div>
              {editingRoute && (
                <Alert
                  type="info"
                  message="Al editar solo se pueden cambiar: Nombre, Distancia y Duración"
                />
              )}
              <div className="modal-actions">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </Button>
                <Button type="submit" loading={loading}>
                  {editingRoute ? 'Actualizar' : 'Crear'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageRoutes
