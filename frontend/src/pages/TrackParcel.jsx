import React, { useState } from 'react'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Loading from '../components/Loading'
import Alert from '../components/Alert'
import parcelService from '../services/parcelService'
import stopService from '../services/stopService'
import {
  FaBox,
  FaQrcode,
  FaPhone,
  FaSearch,
  FaMapMarkerAlt,
  FaClock,
  FaUser,
  FaDollarSign,
  FaImage,
} from 'react-icons/fa'
import { format } from 'date-fns'
import ParcelTimeline from '../components/ParcelTimeline'
import '../styles/TrackParcel.css'

const TrackParcel = () => {
  const [searchType, setSearchType] = useState('code') // 'code' or 'phone'
  const [searchValue, setSearchValue] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [parcels, setParcels] = useState([])
  const [selectedParcel, setSelectedParcel] = useState(null)

  // Función helper para enriquecer parcels con nombres de paradas
  const enrichParcelsWithStopNames = async (parcels) => {
    try {
      // Obtener todos los stopIds únicos
      const stopIds = new Set()
      parcels.forEach(parcel => {
        if (parcel.fromStopId) stopIds.add(parcel.fromStopId)
        if (parcel.toStopId) stopIds.add(parcel.toStopId)
      })
      
      // Obtener todos los stops en paralelo
      const stopsMap = new Map()
      await Promise.all(
        Array.from(stopIds).map(async (stopId) => {
          try {
            const stop = await stopService.getStopById(stopId)
            stopsMap.set(stopId, stop)
          } catch (error) {
            console.error(`Error obteniendo stop ${stopId}:`, error)
          }
        })
      )
      
      // Enriquecer parcels con nombres de paradas
      return parcels.map(parcel => ({
        ...parcel,
        fromStopName: stopsMap.get(parcel.fromStopId)?.name || 'Origen desconocido',
        toStopName: stopsMap.get(parcel.toStopId)?.name || 'Destino desconocido'
      }))
    } catch (error) {
      console.error('Error enriqueciendo parcels:', error)
      return parcels.map(parcel => ({
        ...parcel,
        fromStopName: 'Origen desconocido',
        toStopName: 'Destino desconocido'
      }))
    }
  }

  const handleSearch = async (e) => {
    e.preventDefault()
    setError('')
    setParcels([])
    setSelectedParcel(null)

    if (!searchValue.trim()) {
      setError('Por favor ingrese un valor de búsqueda')
      return
    }

    try {
      setLoading(true)

      if (searchType === 'code') {
        const parcel = await parcelService.getParcelByCode(searchValue.trim())
        const enriched = await enrichParcelsWithStopNames([parcel])
        setParcels(enriched)
        setSelectedParcel(enriched[0])
      } else {
        const results = await parcelService.getParcelsByPhone(searchValue.trim())
        const enriched = await enrichParcelsWithStopNames(results)
        setParcels(enriched)
        if (enriched.length === 1) {
          setSelectedParcel(enriched[0])
        }
      }

    } catch (err) {
      setError(err.response?.data?.message || 'No se encontró la encomienda')
    } finally {
      setLoading(false)
    }
  }

  const getStatusInfo = (status) => {
    const statuses = {
      CREATED: {
        color: '#3498db',
        label: 'Creada',
        description: 'La encomienda ha sido registrada y está pendiente de ser enviada',
        icon: '📝',
      },
      IN_TRANSIT: {
        color: '#f39c12',
        label: 'En Tránsito',
        description: 'La encomienda está en camino hacia su destino',
        icon: '🚚',
      },
      DELIVERED: {
        color: '#2ecc71',
        label: 'Entregada',
        description: 'La encomienda ha sido entregada al destinatario',
        icon: '✅',
      },
      FAILED: {
        color: '#e74c3c',
        label: 'Fallida',
        description: 'Hubo un problema con la entrega',
        icon: '❌',
      },
    }
    return statuses[status] || { color: '#95a5a6', label: status, description: '', icon: '❓' }
  }

  const getStatusBadge = (status) => {
    const info = getStatusInfo(status)
    return (
      <span
        style={{
          backgroundColor: info.color,
          color: 'white',
          padding: '6px 12px',
          borderRadius: '6px',
          fontSize: '0.95rem',
          fontWeight: 'bold',
        }}
      >
        {info.icon} {info.label}
      </span>
    )
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    try {
      return format(new Date(dateString), 'dd/MM/yyyy HH:mm')
    } catch {
      return dateString
    }
  }

  return (
    <div className="track-parcel">
      <h1>Seguimiento de Encomiendas</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      {/* Formulario de búsqueda */}
      <Card>
        <h3>Buscar Encomienda</h3>
        <form onSubmit={handleSearch}>
          <div style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', marginBottom: '8px' }}>Tipo de Búsqueda:</label>
            <div style={{ display: 'flex', gap: '16px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <input
                  type="radio"
                  value="code"
                  checked={searchType === 'code'}
                  onChange={(e) => setSearchType(e.target.value)}
                />
                <FaQrcode /> Por Código
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <input
                  type="radio"
                  value="phone"
                  checked={searchType === 'phone'}
                  onChange={(e) => setSearchType(e.target.value)}
                />
                <FaPhone /> Por Teléfono
              </label>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <Input
                label={searchType === 'code' ? 'Código de Seguimiento' : 'Número de Teléfono'}
                type="text"
                value={searchValue}
                onChange={(e) => setSearchValue(e.target.value)}
                placeholder={searchType === 'code' ? 'PCL-20250118-001' : '1234567890'}
                icon={searchType === 'code' ? <FaQrcode /> : <FaPhone />}
                required
              />
            </div>
            <Button type="submit" loading={loading} icon={<FaSearch />}>
              Buscar
            </Button>
          </div>
        </form>
      </Card>

      {/* Lista de resultados (solo si búsqueda por teléfono) */}
      {searchType === 'phone' && parcels.length > 1 && (
        <Card style={{ marginTop: '20px' }}>
          <h3>Encomiendas Encontradas ({parcels.length})</h3>
          <div style={{ display: 'grid', gap: '12px', marginTop: '12px' }}>
            {parcels.map((parcel) => (
              <Card
                key={parcel.id}
                style={{
                  backgroundColor: selectedParcel?.id === parcel.id ? '#e3f2fd' : '#f8f9fa',
                  cursor: 'pointer',
                  border:
                    selectedParcel?.id === parcel.id ? '2px solid #3498db' : '1px solid #ddd',
                }}
                onClick={() => setSelectedParcel(parcel)}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <div>
                    <strong>
                      <FaBox /> {parcel.code}
                    </strong>
                    <div style={{ fontSize: '0.9rem', color: '#666', marginTop: '4px' }}>
                      {parcel.fromStopName} → {parcel.toStopName}
                    </div>
                  </div>
                  {getStatusBadge(parcel.status)}
                </div>
              </Card>
            ))}
          </div>
        </Card>
      )}

      {/* Detalles de la encomienda */}
      {selectedParcel && (
        <Card style={{ marginTop: '20px' }}>
          <div style={{ textAlign: 'center', marginBottom: '24px' }}>
            <h2>
              <FaBox /> {selectedParcel.code}
            </h2>
            {getStatusBadge(selectedParcel.status)}
            <p style={{ marginTop: '12px', color: '#666' }}>
              {getStatusInfo(selectedParcel.status).description}
            </p>
          </div>

          {/* Timeline visual mejorado */}
          <ParcelTimeline
            status={selectedParcel.status}
            createdAt={selectedParcel.createdAt}
            deliveredAt={selectedParcel.deliveredAt}
          />

          {/* Información detallada */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
            <div>
              <h3>Remitente</h3>
              <div style={{ marginTop: '8px', fontSize: '0.95rem' }}>
                <div style={{ marginBottom: '8px' }}>
                  <FaUser style={{ marginRight: '8px' }} />
                  <strong>Nombre:</strong> {selectedParcel.senderName}
                </div>
                <div>
                  <FaPhone style={{ marginRight: '8px' }} />
                  <strong>Teléfono:</strong> {selectedParcel.senderPhone}
                </div>
              </div>
            </div>

            <div>
              <h3>Destinatario</h3>
              <div style={{ marginTop: '8px', fontSize: '0.95rem' }}>
                <div style={{ marginBottom: '8px' }}>
                  <FaUser style={{ marginRight: '8px' }} />
                  <strong>Nombre:</strong> {selectedParcel.receiverName}
                </div>
                <div>
                  <FaPhone style={{ marginRight: '8px' }} />
                  <strong>Teléfono:</strong> {selectedParcel.receiverPhone}
                </div>
              </div>
            </div>

            <div>
              <h3>Origen y Destino</h3>
              <div style={{ marginTop: '8px', fontSize: '0.95rem' }}>
                <div style={{ marginBottom: '8px' }}>
                  <FaMapMarkerAlt style={{ marginRight: '8px' }} />
                  <strong>Origen:</strong> {selectedParcel.fromStopName}
                </div>
                <div>
                  <FaMapMarkerAlt style={{ marginRight: '8px' }} />
                  <strong>Destino:</strong> {selectedParcel.toStopName}
                </div>
              </div>
            </div>

            <div>
              <h3>Detalles</h3>
              <div style={{ marginTop: '8px', fontSize: '0.95rem' }}>
                <div style={{ marginBottom: '8px' }}>
                  <FaDollarSign style={{ marginRight: '8px' }} />
                  <strong>Precio:</strong> $ {selectedParcel.price?.toLocaleString('es-CO')} COP
                </div>
                <div>
                  <FaClock style={{ marginRight: '8px' }} />
                  <strong>Creado:</strong> {formatDate(selectedParcel.createdAt)}
                </div>
                {selectedParcel.deliveredAt && (
                  <div>
                    <FaClock style={{ marginRight: '8px' }} />
                    <strong>Entregado:</strong> {formatDate(selectedParcel.deliveredAt)}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Foto de comprobante */}
          {selectedParcel.proofPhotoUrl && (
            <div style={{ marginTop: '20px' }}>
              <h3>
                <FaImage /> Comprobante de Entrega
              </h3>
              <div style={{ marginTop: '12px', textAlign: 'center' }}>
                <img
                  src={parcelService.getFileUrl(selectedParcel.proofPhotoUrl)}
                  alt="Comprobante de entrega"
                  style={{
                    maxWidth: '100%',
                    maxHeight: '400px',
                    borderRadius: '8px',
                    border: '2px solid #ddd',
                  }}
                  onError={(e) => {
                    e.target.style.display = 'none'
                  }}
                />
              </div>
            </div>
          )}
        </Card>
      )}

      {loading && <Loading message="Buscando encomienda..." />}
    </div>
  )
}

export default TrackParcel