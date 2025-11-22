import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import parcelService from '../../services/parcelService'
import tripService from '../../services/tripService'
import { FaBox, FaTruck, FaMapMarkerAlt } from 'react-icons/fa'

const DriverParcels = () => {
  const [parcels, setParcels] = useState([])
  const [myTrips, setMyTrips] = useState([])
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => { loadMyTrips() }, [])

  const loadMyTrips = async () => {
    try {
      setLoading(true)
      const trips = await tripService.getActiveAssignedTrips()
      setMyTrips(trips)
      if (trips.length > 0) handleSelectTrip(trips[0])
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
      setParcels(parcelsData)
    } catch (err) {
      setError('Error al cargar encomiendas del viaje')
      setParcels([])
    }
  }

  const getStatusBadge = (status) => {
    const badges = { 
      CREATED: { color: '#3498db', label: 'Creada' }, 
      IN_TRANSIT: { color: '#f39c12', label: 'En Tránsito' }, 
      DELIVERED: { color: '#2ecc71', label: 'Entregada' }, 
      FAILED: { color: '#e74c3c', label: 'Fallida' } 
    }
    const badge = badges[status] || { color: '#95a5a6', label: status }
    return <span style={{backgroundColor: badge.color, color: 'white', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem'}}>{badge.label}</span>
  }

  if (loading && myTrips.length === 0) return <Loading message="Cargando..." />

  return (
    <div style={{maxWidth: '1200px', margin: '0 auto', padding: '24px'}}>
      <div style={{marginBottom: '32px'}}>
        <h1>Encomiendas Asignadas</h1>
        <p style={{color: 'var(--text-secondary)'}}>Ver encomiendas de tus viajes asignados</p>
      </div>
      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {myTrips.length > 0 && (
        <Card style={{marginBottom: '24px'}}>
          <h3>Mis Viajes Activos</h3>
          <div style={{display: 'flex', gap: '12px', flexWrap: 'wrap', marginTop: '16px'}}>
            {myTrips.map(trip => (
              <Button key={trip.id} onClick={() => handleSelectTrip(trip)} variant={selectedTrip?.id === trip.id ? 'primary' : 'secondary'} size="small">
                <FaTruck /> {trip.origin} → {trip.destination} ({trip.date})
              </Button>
            ))}
          </div>
        </Card>
      )}
      {selectedTrip && (
        <Card>
          <h3>Encomiendas del Viaje ({parcels.length})</h3>
          {parcels.length === 0 ? <p style={{textAlign: 'center', padding: '40px', color: 'var(--text-secondary)'}}>No hay encomiendas en este viaje</p> : (
            <div style={{display: 'grid', gap: '12px', marginTop: '16px'}}>
              {parcels.map(parcel => (
                <Card key={parcel.id} style={{backgroundColor: '#f8f9fa'}}>
                  <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'start'}}>
                    <div style={{flex: 1}}>
                      <div style={{marginBottom: '8px'}}>
                        <strong style={{fontSize: '1.1rem'}}><FaBox /> {parcel.code}</strong> {getStatusBadge(parcel.status)}
                      </div>
                      <div style={{color: '#666', fontSize: '0.9rem'}}>
                        <div><strong>De:</strong> {parcel.senderName} ({parcel.senderPhone})</div>
                        <div><strong>Para:</strong> {parcel.receiverName} ({parcel.receiverPhone})</div>
                        <div><FaMapMarkerAlt /> <strong>Tramo:</strong> {parcel.fromStopName} → {parcel.toStopName}</div>
                        <div><strong>Precio:</strong> $ {parcel.price?.toLocaleString('es-CO')} COP</div>
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </Card>
      )}
    </div>
  )
}

export default DriverParcels
