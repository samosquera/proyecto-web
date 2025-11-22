import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import parcelService from '../../services/parcelService'
import tripService from '../../services/tripService'
import {
  FaBox,
  FaQrcode,
  FaCheck,
  FaTimes,
  FaCamera,
  FaSearch,
  FaTruck,
} from 'react-icons/fa'

const DeliverParcel = () => {
  const [parcels, setParcels] = useState([])
  const [myTrips, setMyTrips] = useState([])
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Modal de entrega
  const [showDeliveryModal, setShowDeliveryModal] = useState(false)
  const [selectedParcel, setSelectedParcel] = useState(null)
  const [deliveryOtp, setDeliveryOtp] = useState('')
  const [photoFile, setPhotoFile] = useState(null)
  const [photoPreview, setPhotoPreview] = useState(null)
  const [delivering, setDelivering] = useState(false)

  // Búsqueda por código
  const [searchCode, setSearchCode] = useState('')
  const [searching, setSearching] = useState(false)

  useEffect(() => {
    loadMyTrips()
  }, [])

  const loadMyTrips = async () => {
    try {
      setLoading(true)
      const trips = await tripService.getActiveAssignedTrips()
      setMyTrips(trips)
      if (trips.length > 0) {
        handleSelectTrip(trips[0])
      }
    } catch (err) {
      setError('Error al cargar viajes asignados')
    } finally {
      setLoading(false)
    }
  }

  const handleSelectTrip = async (trip) => {
    setSelectedTrip(trip)
    try {
      const parcelsData = await parcelService.getParcelsByTrip(trip.id)
      setParcels(parcelsData.filter((p) => p.status === 'IN_TRANSIT'))
    } catch (err) {
      setError('Error al cargar encomiendas del viaje')
    }
  }

  const handleSearchByCode = async () => {
    if (!searchCode.trim()) {
      setError('Ingrese un código de seguimiento')
      return
    }

    try {
      setSearching(true)
      const parcel = await parcelService.getParcelByCode(searchCode.trim())
      setSelectedParcel(parcel)
      setShowDeliveryModal(true)
      setSearchCode('')
    } catch (err) {
      setError('Encomienda no encontrada')
    } finally {
      setSearching(false)
    }
  }

  const handleOpenDeliveryModal = (parcel) => {
    setSelectedParcel(parcel)
    setShowDeliveryModal(true)
    setDeliveryOtp('')
    setPhotoFile(null)
    setPhotoPreview(null)
  }

  const handleCloseDeliveryModal = () => {
    setShowDeliveryModal(false)
    setSelectedParcel(null)
    setDeliveryOtp('')
    setPhotoFile(null)
    setPhotoPreview(null)
  }

  const handlePhotoChange = (e) => {
    const file = e.target.files[0]
    if (file) {
      // Validar tamaño (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        setError('La foto no debe exceder 5MB')
        return
      }

      // Validar tipo
      if (!file.type.startsWith('image/')) {
        setError('Solo se permiten archivos de imagen')
        return
      }

      setPhotoFile(file)

      // Preview
      const reader = new FileReader()
      reader.onloadend = () => {
        setPhotoPreview(reader.result)
      }
      reader.readAsDataURL(file)
    }
  }

  const handleConfirmDelivery = async () => {
    if (!deliveryOtp || deliveryOtp.length !== 6) {
      setError('Debe ingresar el OTP de 6 dígitos')
      return
    }

    try {
      setDelivering(true)

      // Marcar como entregado con foto
      await parcelService.markAsDeliveredWithPhoto(selectedParcel.id, deliveryOtp, photoFile)

      setSuccess(`Encomienda ${selectedParcel.code} entregada exitosamente`)
      handleCloseDeliveryModal()

      // Recargar lista
      if (selectedTrip) {
        handleSelectTrip(selectedTrip)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al entregar encomienda. Verifique el OTP.')
    } finally {
      setDelivering(false)
    }
  }

  const handleMarkAsFailed = async (parcelId) => {
    const reason = prompt('Ingrese el motivo del fallo:')
    if (!reason) return

    try {
      await parcelService.markAsFailed(parcelId, reason)
      setSuccess('Encomienda marcada como fallida')
      if (selectedTrip) {
        handleSelectTrip(selectedTrip)
      }
    } catch (err) {
      setError('Error al marcar como fallida')
    }
  }

  const getStatusBadge = (status) => {
    const badges = {
      CREATED: { color: '#3498db', label: 'Creada' },
      IN_TRANSIT: { color: '#f39c12', label: 'En Tránsito' },
      DELIVERED: { color: '#2ecc71', label: 'Entregada' },
      FAILED: { color: '#e74c3c', label: 'Fallida' },
    }
    const badge = badges[status] || { color: '#95a5a6', label: status }
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
    return <Loading message="Cargando..." />
  }

  return (
    <div className="deliver-parcel">
      <h1>Entregar Encomiendas</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Búsqueda por código */}
      <Card style={{ marginBottom: '20px' }}>
        <h3>Buscar por Código de Seguimiento</h3>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
          <div style={{ flex: 1 }}>
            <Input
              label="Código de Seguimiento"
              type="text"
              value={searchCode}
              onChange={(e) => setSearchCode(e.target.value)}
              placeholder="PCL-20250118-001"
              icon={<FaQrcode />}
              onKeyPress={(e) => e.key === 'Enter' && handleSearchByCode()}
            />
          </div>
          <Button onClick={handleSearchByCode} loading={searching} icon={<FaSearch />}>
            Buscar
          </Button>
        </div>
      </Card>

      {/* Selector de viaje */}
      {myTrips.length > 0 && (
        <Card style={{ marginBottom: '20px' }}>
          <h3>Mis Viajes Activos</h3>
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            {myTrips.map((trip) => (
              <Button
                key={trip.id}
                onClick={() => handleSelectTrip(trip)}
                variant={selectedTrip?.id === trip.id ? 'primary' : 'secondary'}
                size="small"
              >
                <FaTruck /> {trip.origin} → {trip.destination} ({trip.date})
              </Button>
            ))}
          </div>
        </Card>
      )}

      {/* Lista de encomiendas */}
      {selectedTrip && (
        <Card>
          <h3>
            Encomiendas en Tránsito ({parcels.length})
          </h3>
          {parcels.length === 0 ? (
            <p>No hay encomiendas en tránsito para este viaje</p>
          ) : (
            <div style={{ display: 'grid', gap: '12px', marginTop: '16px' }}>
              {parcels.map((parcel) => (
                <Card key={parcel.id} style={{ backgroundColor: '#f8f9fa' }}>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'start',
                    }}
                  >
                    <div style={{ flex: 1 }}>
                      <div style={{ marginBottom: '8px' }}>
                        <strong style={{ fontSize: '1.1rem' }}>
                          <FaBox /> {parcel.code}
                        </strong>
                        {' '}
                        {getStatusBadge(parcel.status)}
                      </div>
                      <div style={{ color: '#666', fontSize: '0.9rem' }}>
                        <div>
                          <strong>De:</strong> {parcel.senderName} ({parcel.senderPhone})
                        </div>
                        <div>
                          <strong>Para:</strong> {parcel.receiverName} ({parcel.receiverPhone})
                        </div>
                        <div>
                          <strong>Tramo:</strong> {parcel.fromStopName} → {parcel.toStopName}
                        </div>
                        <div>
                          <strong>Precio:</strong> $ {parcel.price?.toLocaleString('es-CO')} COP
                        </div>
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <Button
                        onClick={() => handleOpenDeliveryModal(parcel)}
                        variant="success"
                        size="small"
                        icon={<FaCheck />}
                      >
                        Entregar
                      </Button>
                      <Button
                        onClick={() => handleMarkAsFailed(parcel.id)}
                        variant="danger"
                        size="small"
                        icon={<FaTimes />}
                      >
                        Falló
                      </Button>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </Card>
      )}

      {/* Modal de entrega */}
      {showDeliveryModal && selectedParcel && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={handleCloseDeliveryModal}
        >
          <div
            style={{
              backgroundColor: 'white',
              borderRadius: '12px',
              padding: '24px',
              maxWidth: '600px',
              width: '90%',
              maxHeight: '90vh',
              overflow: 'auto',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginBottom: '16px' }}>Confirmar Entrega</h2>

            <Card style={{ marginBottom: '16px', backgroundColor: '#f8f9fa' }}>
              <div>
                <strong>Código:</strong> {selectedParcel.code}
              </div>
              <div>
                <strong>Destinatario:</strong> {selectedParcel.receiverName}
              </div>
              <div>
                <strong>Teléfono:</strong> {selectedParcel.receiverPhone}
              </div>
              <div>
                <strong>Destino:</strong> {selectedParcel.toStopName}
              </div>
            </Card>

            <Input
              label="OTP de Entrega"
              type="text"
              value={deliveryOtp}
              onChange={(e) => setDeliveryOtp(e.target.value)}
              placeholder="123456"
              maxLength="6"
              pattern="\d{6}"
              icon={<FaQrcode />}
            />

            <div style={{ marginTop: '16px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                <FaCamera /> Foto de Comprobante (Opcional)
              </label>
              <input
                type="file"
                accept="image/*"
                capture="environment"
                onChange={handlePhotoChange}
                style={{
                  padding: '8px',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  width: '100%',
                }}
              />
              {photoPreview && (
                <div style={{ marginTop: '12px' }}>
                  <img
                    src={photoPreview}
                    alt="Preview"
                    style={{
                      maxWidth: '100%',
                      maxHeight: '300px',
                      borderRadius: '8px',
                      border: '2px solid #ddd',
                    }}
                  />
                </div>
              )}
            </div>

            <div
              style={{
                marginTop: '16px',
                padding: '12px',
                backgroundColor: '#fff3cd',
                borderRadius: '8px',
                fontSize: '0.9rem',
              }}
            >
              <strong>Importante:</strong> Solicite al destinatario el OTP de 6 dígitos que recibió.
              La encomienda solo se entregará con el OTP correcto.
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
              <Button
                onClick={handleConfirmDelivery}
                variant="success"
                fullWidth
                loading={delivering}
                disabled={delivering || deliveryOtp.length !== 6}
                icon={<FaCheck />}
              >
                {delivering ? 'Procesando...' : 'Confirmar Entrega'}
              </Button>
              <Button
                onClick={handleCloseDeliveryModal}
                variant="secondary"
                fullWidth
                disabled={delivering}
              >
                Cancelar
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default DeliverParcel
