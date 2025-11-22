import React, { useState, useEffect } from 'react'
import parcelService from '../../services/parcelService'
import stopService from '../../services/stopService'
import routeService from '../../services/routeService'
import Card from '../../components/Card'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaSearch, FaEye, FaBox } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const ManageParcels = () => {
  const [parcels, setParcels] = useState([])
  const [filteredParcels, setFilteredParcels] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [selectedParcel, setSelectedParcel] = useState(null)

  useEffect(() => {
    loadParcels()
  }, [])

  useEffect(() => {
    filterParcels()
  }, [parcels, searchTerm, statusFilter])

  const loadParcels = async () => {
    try {
      setLoading(true)
      const data = await parcelService.getAllParcels()

      // Enriquecer parcels con información de ruta
      const routeCache = {} // Cache para evitar peticiones duplicadas
      const enrichedParcels = await Promise.all(
        data.map(async (parcel) => {
          try {
            // Si no hay fromStopId, devolver el parcel sin modificar
            if (!parcel.fromStopId) {
              return parcel
            }

            // Obtener información del stop
            const stop = await stopService.getStopById(parcel.fromStopId)
            const routeId = stop.routeId

            // Si ya tenemos la ruta en cache, usarla
            if (!routeCache[routeId]) {
              const route = await routeService.getRouteById(routeId)
              routeCache[routeId] = {
                id: route.id,
                origin: route.origin,
                destination: route.destination,
                code: route.code,
                name: route.name
              }
            }

            // Enriquecer el parcel con la información de ruta
            return {
              ...parcel,
              route: routeCache[routeId]
            }
          } catch (err) {
            console.error(`Error al cargar ruta para parcel ${parcel.id}:`, err)
            // Si falla, devolver el parcel sin la información de ruta
            return parcel
          }
        })
      )

      setParcels(enrichedParcels)
    } catch (err) {
      setError('Error al cargar encomiendas: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const filterParcels = () => {
    let filtered = parcels

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((p) => p.status === statusFilter)
    }

    if (searchTerm) {
      filtered = filtered.filter(
        (p) =>
          p.senderName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          p.receiverName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          p.senderPhone?.includes(searchTerm) ||
          p.receiverPhone?.includes(searchTerm) ||
          p.code?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    setFilteredParcels(filtered)
  }

  const handleViewDetails = (parcel) => {
    setSelectedParcel(parcel)
    setShowDetailsModal(true)
  }

  const getStatusLabel = (status) => {
    const labels = {
      PENDING: 'Pendiente',
      IN_TRANSIT: 'En Tránsito',
      DELIVERED: 'Entregado',
      FAILED: 'Fallido',
    }
    return labels[status] || status
  }

  if (loading && parcels.length === 0) {
    return <Loading message="Cargando encomiendas..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Gestión de Encomiendas</h1>
          <p>Visualice y administre las encomiendas</p>
        </div>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="filters-card">
        <div className="filters-grid">
          <Input
            placeholder="Buscar por remitente, destinatario o código..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<FaSearch />}
          />
          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { value: 'ALL', label: 'Todos los estados' },
              { value: 'PENDING', label: 'Pendientes' },
              { value: 'IN_TRANSIT', label: 'En Tránsito' },
              { value: 'DELIVERED', label: 'Entregados' },
              { value: 'FAILED', label: 'Fallidos' },
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
                <th>Código</th>
                <th>Remitente</th>
                <th>Destinatario</th>
                <th>Ruta</th>
                <th>Estado</th>
                <th>Precio</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredParcels.length === 0 ? (
                <tr>
                  <td colSpan="8" className="text-center">
                    No se encontraron encomiendas
                  </td>
                </tr>
              ) : (
                filteredParcels.map((parcel) => (
                  <tr key={parcel.id}>
                    <td>{parcel.id}</td>
                    <td>
                      <div className="user-cell">
                        <FaBox />
                        <span>{parcel.code}</span>
                      </div>
                    </td>
                    <td>
                      {parcel.senderName}
                      <br />
                      <small>{parcel.senderPhone}</small>
                    </td>
                    <td>
                      {parcel.receiverName}
                      <br />
                      <small>{parcel.receiverPhone}</small>
                    </td>
                    <td>
                      {parcel.route?.origin} → {parcel.route?.destination}
                    </td>
                    <td>
                      <span className={`badge badge-${parcel.status?.toLowerCase()}`}>
                        {getStatusLabel(parcel.status)}
                      </span>
                    </td>
                    <td>$ {parcel.price?.toLocaleString('es-CO')} COP</td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="btn-icon"
                          onClick={() => handleViewDetails(parcel)}
                          title="Ver detalles"
                        >
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

      {showDetailsModal && selectedParcel && (
        <div className="modal-overlay" onClick={() => setShowDetailsModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Detalles de Encomienda</h2>
            <div className="parcel-details">
              <div className="detail-section">
                <h3>Información General</h3>
                <p>
                  <strong>Código:</strong> {selectedParcel.code}
                </p>
                <p>
                  <strong>Estado:</strong> {getStatusLabel(selectedParcel.status)}
                </p>
                <p>
                  <strong>Precio:</strong> $ {selectedParcel.price?.toLocaleString('es-CO')} COP
                </p>
                <p>
                  <strong>Peso:</strong> {selectedParcel.weight} kg
                </p>
              </div>

              <div className="detail-section">
                <h3>Remitente</h3>
                <p>
                  <strong>Nombre:</strong> {selectedParcel.senderName}
                </p>
                <p>
                  <strong>Teléfono:</strong> {selectedParcel.senderPhone}
                </p>
              </div>

              <div className="detail-section">
                <h3>Destinatario</h3>
                <p>
                  <strong>Nombre:</strong> {selectedParcel.receiverName}
                </p>
                <p>
                  <strong>Teléfono:</strong> {selectedParcel.receiverPhone}
                </p>
                <p>
                  <strong>Dirección:</strong> {selectedParcel.receiverAddress}
                </p>
              </div>

              <div className="detail-section">
                <h3>Ruta y Viaje</h3>
                <p>
                  <strong>Ruta:</strong> {selectedParcel.route?.origin} →{' '}
                  {selectedParcel.route?.destination}
                </p>
                {selectedParcel.trip && (
                  <p>
                    <strong>Viaje ID:</strong> {selectedParcel.trip.id}
                  </p>
                )}
              </div>

              {selectedParcel.description && (
                <div className="detail-section">
                  <h3>Descripción</h3>
                  <p>{selectedParcel.description}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageParcels
