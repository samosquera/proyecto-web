import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import tripService from '../../services/tripService'
import ticketService from '../../services/ticketService'
import {
  FaBus,
  FaPlayCircle,
  FaStopCircle,
  FaCheckCircle,
  FaUserCheck,
  FaUserTimes,
  FaMapMarkerAlt,
  FaClock,
} from 'react-icons/fa'

const DispatcherDispatch = () => {
  const [trips, setTrips] = useState([])
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [ticketsLoading, setTicketsLoading] = useState(false)

  useEffect(() => {
    loadTrips()
  }, [])

  const loadTrips = async () => {
    try {
      setLoading(true)
      const tripsData = await tripService.getTodayActiveTrips()
      setTrips(tripsData)
    } catch (err) {
      setError('Error al cargar viajes: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const loadTickets = async (tripId) => {
    try {
      setTicketsLoading(true)
      const ticketsData = await ticketService.getTicketsByTrip(tripId)
      setTickets(ticketsData)
    } catch (err) {
      setError('Error al cargar boletos: ' + (err.response?.data?.message || err.message))
    } finally {
      setTicketsLoading(false)
    }
  }

  const handleSelectTrip = async (trip) => {
    setSelectedTrip(trip)
    await loadTickets(trip.id)
  }

  const handleOpenBoarding = async (tripId) => {
    if (!window.confirm('¿Abrir abordaje para este viaje?')) return

    try {
      await tripService.openBoarding(tripId)
      setSuccess('Abordaje abierto exitosamente')
      loadTrips()
      if (selectedTrip?.id === tripId) {
        const updatedTrip = await tripService.getTripById(tripId)
        setSelectedTrip(updatedTrip)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al abrir abordaje')
    }
  }

  const handleCloseBoarding = async (tripId) => {
    if (!window.confirm('¿Cerrar abordaje y marcar como partido?')) return

    try {
      await tripService.closeBoarding(tripId)
      setSuccess('Viaje marcado como partido exitosamente')
      loadTrips()
      if (selectedTrip?.id === tripId) {
        const updatedTrip = await tripService.getTripById(tripId)
        setSelectedTrip(updatedTrip)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cerrar abordaje')
    }
  }

  const handleDepartTrip = async (tripId) => {
    if (!window.confirm('¿Marcar este viaje como partido?')) return

    try {
      await tripService.departTrip(tripId)
      setSuccess('Viaje marcado como partido')
      loadTrips()
      if (selectedTrip?.id === tripId) {
        const updatedTrip = await tripService.getTripById(tripId)
        setSelectedTrip(updatedTrip)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar como partido')
    }
  }

  const handleArriveTrip = async (tripId) => {
    if (!window.confirm('¿Marcar este viaje como llegado?')) return

    try {
      await tripService.arriveTrip(tripId)
      setSuccess('Viaje marcado como llegado')
      loadTrips()
      setSelectedTrip(null)
      setTickets([])
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar como llegado')
    }
  }

  const handleMarkAsBoarded = async (ticketId) => {
    try {
      await ticketService.markAsUsed(ticketId)
      setSuccess('Pasajero marcado como abordado')
      if (selectedTrip) {
        await loadTickets(selectedTrip.id)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar como abordado')
    }
  }

  const handleMarkAsNoShow = async (ticketId) => {
    if (!window.confirm('¿Marcar este pasajero como no presentado?')) return

    try {
      await ticketService.markAsNoShow(ticketId)
      setSuccess('Pasajero marcado como no presentado')
      if (selectedTrip) {
        await loadTickets(selectedTrip.id)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al marcar como no presentado')
    }
  }

  const getStatusBadge = (status) => {
    const badges = {
      SCHEDULED: { color: '#3498db', label: 'Programado' },
      BOARDING: { color: '#f39c12', label: 'Abordando' },
      DEPARTED: { color: '#2ecc71', label: 'En ruta' },
      ARRIVED: { color: '#95a5a6', label: 'Llegado' },
      CANCELLED: { color: '#e74c3c', label: 'Cancelado' },
    }
    const badge = badges[status] || { color: '#95a5a6', label: status }
    return (
      <span
        style={{
          backgroundColor: badge.color,
          color: 'white',
          padding: '6px 12px',
          borderRadius: '6px',
          fontSize: '0.9rem',
          fontWeight: 'bold',
        }}
      >
        {badge.label}
      </span>
    )
  }

  const getTicketStatusBadge = (status) => {
    const badges = {
      PENDING_PAYMENT: { color: '#e67e22', label: 'Pago Pendiente' },
      SOLD: { color: '#3498db', label: 'Confirmado' },
      CANCELLED: { color: '#e74c3c', label: 'Cancelado' },
      NO_SHOW: { color: '#95a5a6', label: 'No Show' },
      USED: { color: '#2ecc71', label: 'Abordado' },
    }
    const badge = badges[status] || { color: '#95a5a6', label: status }
    return (
      <span
        style={{
          backgroundColor: badge.color,
          color: 'white',
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '0.8rem',
        }}
      >
        {badge.label}
      </span>
    )
  }

  const getTicketStats = () => {
    const total = tickets.length
    const sold = tickets.filter((t) => t.status === 'SOLD').length
    const used = tickets.filter((t) => t.status === 'USED').length
    const noShow = tickets.filter((t) => t.status === 'NO_SHOW').length
    return { total, sold, used, noShow, pending: sold - used - noShow }
  }

  if (loading) {
    return <Loading message="Cargando panel de despacho..." />
  }

  return (
    <div className="dispatcher-dispatch">
      <h1>Panel de Despacho</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <div className="dispatch-layout" style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '20px' }}>
        {/* Lista de viajes */}
        <div className="trips-panel">
          <h2>Viajes Activos ({trips.length})</h2>
          {trips.length === 0 ? (
            <Card>
              <p>No hay viajes activos</p>
            </Card>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {trips.map((trip) => (
                <Card
                  key={trip.id}
                  className={`trip-card ${selectedTrip?.id === trip.id ? 'selected' : ''}`}
                  onClick={() => handleSelectTrip(trip)}
                  style={{
                    cursor: 'pointer',
                    border: selectedTrip?.id === trip.id ? '2px solid #3498db' : '1px solid #ddd',
                  }}
                >
                  <div style={{ marginBottom: '8px' }}>
                    <strong>
                      {trip.origin} → {trip.destination}
                    </strong>
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#666', marginBottom: '8px' }}>
                    <div>
                      <FaClock style={{ marginRight: '4px' }} />
                      Salida: {trip.departureAt}
                    </div>
                    <div>
                      <FaBus style={{ marginRight: '4px' }} />
                      Bus: {trip.busPlate}
                    </div>
                  </div>
                  {getStatusBadge(trip.status)}
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Panel de detalles del viaje */}
        <div className="trip-details-panel">
          {!selectedTrip ? (
            <Card>
              <p>Seleccione un viaje para ver los detalles</p>
            </Card>
          ) : (
            <>
              <Card className="trip-header-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                  <div>
                    <h2>
                      {selectedTrip.origin} → {selectedTrip.destination}
                    </h2>
                    <div style={{ marginTop: '8px', color: '#666' }}>
                      <div>
                        <FaBus style={{ marginRight: '8px' }} />
                        Bus: {selectedTrip.busPlate} - Capacidad: {selectedTrip.capacity}
                      </div>
                      <div>
                        <FaClock style={{ marginRight: '8px' }} />
                        Salida: {selectedTrip.departureAt} | Llegada: {selectedTrip.arrivalEta}
                      </div>
                      <div>
                        <FaMapMarkerAlt style={{ marginRight: '8px' }} />
                        Fecha: {selectedTrip.date}
                      </div>
                    </div>
                  </div>
                  {getStatusBadge(selectedTrip.status)}
                </div>

                {/* Acciones de despacho */}
                <div style={{ marginTop: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                  {selectedTrip.status === 'SCHEDULED' && (
                    <Button
                      onClick={() => handleOpenBoarding(selectedTrip.id)}
                      variant="primary"
                      icon={<FaPlayCircle />}
                    >
                      Abrir Abordaje
                    </Button>
                  )}

                  {selectedTrip.status === 'BOARDING' && (
                    <>
                      <Button
                        onClick={() => handleCloseBoarding(selectedTrip.id)}
                        variant="success"
                        icon={<FaCheckCircle />}
                      >
                        Cerrar Abordaje y Partir
                      </Button>
                      <Button
                        onClick={() => handleDepartTrip(selectedTrip.id)}
                        variant="warning"
                        icon={<FaBus />}
                      >
                        Marcar como Partido
                      </Button>
                    </>
                  )}

                  {selectedTrip.status === 'DEPARTED' && (
                    <Button
                      onClick={() => handleArriveTrip(selectedTrip.id)}
                      variant="success"
                      icon={<FaCheckCircle />}
                    >
                      Marcar como Llegado
                    </Button>
                  )}
                </div>
              </Card>

              {/* Estadísticas de tickets */}
              {tickets.length > 0 && (
                <Card style={{ marginTop: '20px' }}>
                  <h3>Estadísticas de Pasajeros</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px', marginTop: '12px' }}>
                    {(() => {
                      const stats = getTicketStats()
                      return (
                        <>
                          <div style={{ textAlign: 'center', padding: '12px', backgroundColor: '#ecf0f1', borderRadius: '8px' }}>
                            <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{stats.total}</div>
                            <div style={{ fontSize: '0.9rem', color: '#666' }}>Total</div>
                          </div>
                          <div style={{ textAlign: 'center', padding: '12px', backgroundColor: '#d5f4e6', borderRadius: '8px' }}>
                            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#2ecc71' }}>{stats.used}</div>
                            <div style={{ fontSize: '0.9rem', color: '#666' }}>Abordados</div>
                          </div>
                          <div style={{ textAlign: 'center', padding: '12px', backgroundColor: '#fef5e7', borderRadius: '8px' }}>
                            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f39c12' }}>{stats.pending}</div>
                            <div style={{ fontSize: '0.9rem', color: '#666' }}>Pendientes</div>
                          </div>
                          <div style={{ textAlign: 'center', padding: '12px', backgroundColor: '#fadbd8', borderRadius: '8px' }}>
                            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#e74c3c' }}>{stats.noShow}</div>
                            <div style={{ fontSize: '0.9rem', color: '#666' }}>No Show</div>
                          </div>
                        </>
                      )
                    })()}
                  </div>
                </Card>
              )}

              {/* Lista de pasajeros */}
              <Card style={{ marginTop: '20px' }}>
                <h3>Lista de Pasajeros ({tickets.length})</h3>
                {ticketsLoading ? (
                  <Loading message="Cargando pasajeros..." />
                ) : tickets.length === 0 ? (
                  <p>No hay boletos vendidos para este viaje</p>
                ) : (
                  <div style={{ marginTop: '12px' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                      <thead>
                        <tr style={{ borderBottom: '2px solid #ddd' }}>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Asiento</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Pasajero</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Tramo</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Estado</th>
                          <th style={{ padding: '12px', textAlign: 'left' }}>Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {tickets.map((ticket) => (
                          <tr key={ticket.id} style={{ borderBottom: '1px solid #eee' }}>
                            <td style={{ padding: '12px' }}>
                              <strong>{ticket.seatNumber}</strong>
                            </td>
                            <td style={{ padding: '12px' }}>{ticket.passengerName || 'Sin nombre'}</td>
                            <td style={{ padding: '12px', fontSize: '0.9rem', color: '#666' }}>
                              {ticket.fromStopName || 'N/A'} → {ticket.toStopName || 'N/A'}
                            </td>
                            <td style={{ padding: '12px' }}>{getTicketStatusBadge(ticket.status)}</td>
                            <td style={{ padding: '12px' }}>
                              {ticket.status === 'SOLD' && selectedTrip.status === 'BOARDING' && (
                                <div style={{ display: 'flex', gap: '8px' }}>
                                  <Button
                                    onClick={() => handleMarkAsBoarded(ticket.id)}
                                    variant="success"
                                    size="small"
                                    icon={<FaUserCheck />}
                                  >
                                    Abordar
                                  </Button>
                                  <Button
                                    onClick={() => handleMarkAsNoShow(ticket.id)}
                                    variant="danger"
                                    size="small"
                                    icon={<FaUserTimes />}
                                  >
                                    No Show
                                  </Button>
                                </div>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </Card>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default DispatcherDispatch
