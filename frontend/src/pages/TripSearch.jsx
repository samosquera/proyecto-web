import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Select from '../components/Select'
import Loading from '../components/Loading'
import Alert from '../components/Alert'
import tripService from '../services/tripService'
import routeService from '../services/routeService'
import seatService from '../services/seatService'
import seatHoldService from '../services/seatHoldService'
import ticketService from '../services/ticketService'
import fareRuleService from '../services/fareRuleService'
import { FaMapMarkerAlt, FaCalendar, FaBus, FaClock, FaDollarSign, FaUsers, FaRoute } from 'react-icons/fa'
import { format } from 'date-fns'
import '../styles/TripSearch.css'

const TripSearch = () => {
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const [origins, setOrigins] = useState([])
  const [destinations, setDestinations] = useState([])
  const [trips, setTrips] = useState([])
  const [loading, setLoading] = useState(false)
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [availableSeats, setAvailableSeats] = useState([])
  const [allSeats, setAllSeats] = useState([]) // Todas las sillas con su estado
  const [selectedSeat, setSelectedSeat] = useState(null)
  const [routeStops, setRouteStops] = useState([])
  const [selectedFromStop, setSelectedFromStop] = useState(null)
  const [selectedToStop, setSelectedToStop] = useState(null)
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [calculatedPrice, setCalculatedPrice] = useState(null)
  const [loadingPrice, setLoadingPrice] = useState(false)

  const [searchParams, setSearchParams] = useState({
    origin: '',
    destination: '',
    date: format(new Date(), 'yyyy-MM-dd'),
  })

  useEffect(() => {
    loadOrigins()
  }, [])

  useEffect(() => {
    if (searchParams.origin) {
      loadDestinations(searchParams.origin)
    }
  }, [searchParams.origin])

  const loadOrigins = async () => {
    try {
      setLoading(true)
      const data = await routeService.getOrigins()
      setOrigins(data.map((origin) => ({ value: origin, label: origin })))
    } catch (err) {
      setError('Error al cargar orígenes')
    } finally {
      setLoading(false)
    }
  }

  const loadDestinations = async (origin) => {
    try {
      const data = await routeService.getDestinationsByOrigin(origin)
      setDestinations(data.map((dest) => ({ value: dest, label: dest })))
    } catch (err) {
      setError('Error al cargar destinos')
    }
  }

  const handleSearchChange = (e) => {
    const { name, value } = e.target
    setSearchParams((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSearch = async (e) => {
    e.preventDefault()
    setError('')
    setTrips([])
    setSelectedTrip(null)

    if (!searchParams.origin || !searchParams.destination || !searchParams.date) {
      setError('Por favor complete todos los campos')
      return
    }

    try {
      setSearching(true)
      const data = await tripService.filterTrips(searchParams)
      setTrips(data)

      if (data.length === 0) {
        setError('No se encontraron viajes para los criterios seleccionados')
      }
    } catch (err) {
      setError('Error al buscar viajes')
    } finally {
      setSearching(false)
    }
  }

  const handleSelectTrip = async (trip) => {
    setSelectedTrip(trip)
    setSelectedSeat(null)
    setSelectedFromStop(null)
    setSelectedToStop(null)
    setAvailableSeats([])
    setAllSeats([])
    setRouteStops([]) // IMPORTANTE: Limpiar las paradas anteriores
    setError('')

    try {
      setLoading(true)
      // Solo obtener paradas de la ruta - NO los asientos todavía
      if (trip.routeId) {
        const stops = await routeService.getRouteStops(trip.routeId)
        setRouteStops(stops)
      } else {
        setError('Este viaje no tiene una ruta asignada')
      }
    } catch (err) {
      console.error('Error al cargar información del viaje:', err)
      setError('Error al cargar información del viaje: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  // Calcular precio cuando se seleccionan los stops
  useEffect(() => {
    const calculatePrice = async () => {
      if (selectedTrip && selectedFromStop && selectedToStop && selectedTrip.routeId) {
        try {
          setLoadingPrice(true)
          const fareRule = await fareRuleService.getFareRuleForSegment(
            selectedTrip.routeId,
            selectedFromStop.id,
            selectedToStop.id
          )
          setCalculatedPrice(fareRule.price)
        } catch (err) {
          console.error('Error al calcular precio:', err)
          setError('No se encontró tarifa para este segmento. Por favor contacte al administrador.')
          setCalculatedPrice(null)
        } finally {
          setLoadingPrice(false)
        }
      } else {
        setCalculatedPrice(null)
      }
    }

    calculatePrice()
  }, [selectedTrip, selectedFromStop, selectedToStop])

  // Cargar asientos cuando se seleccionan las paradas
  useEffect(() => {
    const loadSeatsForSegment = async () => {
      if (selectedTrip && selectedFromStop && selectedToStop) {
        try {
          setLoading(true)
          setError('')
          setSelectedSeat(null) // Limpiar selección al cambiar segmento

          const response = await seatService.getSeatsBySegment(
            selectedTrip.id,
            selectedFromStop.id,
            selectedToStop.id
          )

          // response.seats contiene TODOS los asientos con sus estados
          // SeatStatusResponse: { seatId, seatNumber, isOccupied, isHeld, available }
          setAllSeats(response.seats)
          setAvailableSeats(response.seats.filter((seat) => seat.available))

          const availableCount = response.seats.filter((seat) => seat.available).length
          if (availableCount === 0) {
            setError('No hay asientos disponibles para este segmento. Por favor, seleccione otro tramo.')
          }
        } catch (err) {
          console.error('Error al cargar asientos del segmento:', err)
          setError('Error al cargar asientos disponibles: ' + (err.response?.data?.message || err.message))
          setAllSeats([])
          setAvailableSeats([])
        } finally {
          setLoading(false)
        }
      } else {
        // Limpiar asientos si no hay segmento completo seleccionado
        setAllSeats([])
        setAvailableSeats([])
      }
    }

    loadSeatsForSegment()
  }, [selectedTrip, selectedFromStop, selectedToStop])

  const handleBookTicket = async () => {
    // Verificar autenticación PRIMERO
    if (!isAuthenticated()) {
      setError('Debes iniciar sesión para comprar un boleto. Por favor, haz clic en "Iniciar Sesión" en la barra lateral.')
      return
    }

    // Validar que tengamos el ID del usuario
    if (!user || !user.id) {
      setError('No se pudo obtener la información del usuario. Por favor, vuelve a iniciar sesión.')
      return
    }

    if (!selectedSeat) {
      setError('Por favor seleccione un asiento')
      return
    }

    if (!selectedFromStop || !selectedToStop) {
      setError('Por favor seleccione las paradas de origen y destino')
      return
    }

    if (selectedFromStop.stopOrder >= selectedToStop.stopOrder) {
      setError('La parada de destino debe ser posterior a la de origen')
      return
    }

    try {
      setLoading(true)

      // PASO 1: Crear SeatHold primero (requerido por el backend)
      const holdData = {
        tripId: selectedTrip.id,
        seatNumber: selectedSeat.seatNumber,
        fromStopId: selectedFromStop.id,
        toStopId: selectedToStop.id,
      }

      await seatHoldService.createSeatHold(holdData)

      // PASO 2: Crear Ticket (el backend verificará que existe el hold)
      const ticketData = {
        tripId: selectedTrip.id,
        passengerId: user.id, // ID del usuario autenticado desde el contexto
        fromStopId: selectedFromStop.id,
        toStopId: selectedToStop.id,
        seatNumber: selectedSeat.seatNumber, // Usar seatNumber del DTO SeatStatusResponse
        paymentMethod: paymentMethod,
      }

      const ticket = await ticketService.createTicket(ticketData)

      // PASO 3: Navegar a la vista de pago con los datos del ticket
      navigate('/payment', {
        state: {
          ticketId: ticket.id,
          paymentMethod: paymentMethod,
          ticketData: {
            origin: selectedTrip.origin,
            destination: selectedTrip.destination,
            departureAt: selectedTrip.departureAt,
            seatNumber: selectedSeat.seatNumber,
          },
        },
      })
    } catch (err) {
      setError(err.response?.data?.message || 'Error al reservar boleto')
    } finally {
      setLoading(false)
    }
  }

  const getTripStatusLabel = (status) => {
    const labels = {
      SCHEDULED: 'Programado',
      BOARDING: 'Abordando',
      DEPARTED: 'En ruta',
      ARRIVED: 'Llegado',
      CANCELLED: 'Cancelado',
    }
    return labels[status] || status
  }

  if (loading) {
    return <Loading message="Cargando..." />
  }

  return (
    <div className="trip-search">
      <h1>Buscar Viajes</h1>

      {!isAuthenticated() && (
        <Alert
          type="info"
          message="Puedes buscar y explorar viajes sin necesidad de iniciar sesión. Sin embargo, para comprar un boleto deberás autenticarte primero."
        />
      )}

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="search-form-card">
        <form onSubmit={handleSearch} className="search-form">
          <Select
            label="Origen"
            name="origin"
            value={searchParams.origin}
            onChange={handleSearchChange}
            options={origins}
            placeholder="Seleccione origen"
            required
            icon={<FaMapMarkerAlt />}
          />

          <Select
            label="Destino"
            name="destination"
            value={searchParams.destination}
            onChange={handleSearchChange}
            options={destinations}
            placeholder="Seleccione destino"
            required
            disabled={!searchParams.origin}
            icon={<FaMapMarkerAlt />}
          />

          <Input
            label="Fecha"
            type="date"
            name="date"
            value={searchParams.date}
            onChange={handleSearchChange}
            required
          />

          <Button type="submit" loading={searching} fullWidth>
            Buscar Viajes
          </Button>
        </form>
      </Card>

      {trips.length > 0 && (
        <div className="trips-results">
          <h2>Viajes Disponibles</h2>
          <div className="trips-grid">
            {trips.map((trip) => (
              <Card key={trip.id} className="trip-card">
                <div className="trip-info">
                  <div className="trip-route">
                    <FaMapMarkerAlt />
                    <span>
                      {trip.origin || 'N/A'} → {trip.destination || 'N/A'}
                    </span>
                  </div>
                  <div className="trip-detail">
                    <FaClock />
                    <span>Salida: {trip.departureAt || 'N/A'}</span>
                  </div>
                  <div className="trip-detail">
                    <FaBus />
                    <span>Bus: {trip.busPlate || 'Sin asignar'}</span>
                  </div>
                  <div className="trip-detail">
                    <FaUsers />
                    <span>Capacidad: {trip.capacity || 'N/A'} asientos</span>
                  </div>
                  <div className="trip-status">
                    Estado: {getTripStatusLabel(trip.status)}
                  </div>
                </div>
                <Button onClick={() => handleSelectTrip(trip)} variant="primary">
                  Seleccionar
                </Button>
              </Card>
            ))}
          </div>
        </div>
      )}

      {selectedTrip && (
        <div className="trip-booking-section">
          <h2>Reserva tu boleto</h2>
          <p className="trip-details-header">
            <strong>{selectedTrip.origin}</strong> → <strong>{selectedTrip.destination}</strong> | Bus: {selectedTrip.busPlate} | Salida: {selectedTrip.departureAt}
          </p>

          {loading && routeStops.length === 0 ? (
            <Card>
              <Loading message="Cargando paradas de la ruta..." />
            </Card>
          ) : routeStops.length === 0 ? (
            <Card>
              <Alert
                type="error"
                message="No se pudieron cargar las paradas de esta ruta. Por favor, intente con otro viaje."
              />
            </Card>
          ) : (
            <div className="booking-container">
              {/* Columna izquierda: Timeline de paradas */}
              <Card className="timeline-card">
                <h3>Seleccione su tramo</h3>
                <div className="route-timeline">
                {routeStops.map((stop, index) => (
                  <div
                    key={stop.id}
                    className={`timeline-stop ${
                      selectedFromStop?.id === stop.id ? 'selected-from' : ''
                    } ${selectedToStop?.id === stop.id ? 'selected-to' : ''}`}
                  >
                    <div className="timeline-marker">
                      <div className="timeline-dot">{index + 1}</div>
                      {index < routeStops.length - 1 && <div className="timeline-line"></div>}
                    </div>
                    <div className="timeline-content">
                      <div className="stop-name">{stop.name}</div>
                      <div className="stop-actions">
                        <button
                          type="button"
                          className={`stop-btn ${selectedFromStop?.id === stop.id ? 'active' : ''}`}
                          onClick={() => {
                            setSelectedFromStop(stop)
                            setSelectedSeat(null)
                            if (selectedToStop && stop.stopOrder >= selectedToStop.stopOrder) {
                              setSelectedToStop(null)
                            }
                          }}
                          disabled={selectedToStop && stop.stopOrder >= selectedToStop.stopOrder}
                        >
                          Origen
                        </button>
                        <button
                          type="button"
                          className={`stop-btn ${selectedToStop?.id === stop.id ? 'active' : ''}`}
                          onClick={() => {
                            setSelectedToStop(stop)
                            setSelectedSeat(null)
                          }}
                          disabled={!selectedFromStop || stop.stopOrder <= selectedFromStop.stopOrder}
                        >
                          Destino
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              {selectedFromStop && selectedToStop && (
                <div className="segment-info">
                  <strong>Tramo:</strong> {selectedFromStop.name} → {selectedToStop.name}
                </div>
              )}
            </Card>

            {/* Columna derecha: Mapa de asientos */}
            <div className="seat-selection-container">
              {!selectedFromStop || !selectedToStop ? (
                <Card className="seat-selection-placeholder">
                  <div className="placeholder-content">
                    <FaMapMarkerAlt size={48} />
                    <h3>Seleccione su tramo</h3>
                    <p>Por favor, seleccione las paradas de origen y destino para ver los asientos disponibles</p>
                  </div>
                </Card>
              ) : loading ? (
                <Card className="seat-selection-placeholder">
                  <Loading message="Cargando asientos disponibles..." />
                </Card>
              ) : allSeats.length === 0 ? (
                <Card className="seat-selection-placeholder">
                  <Alert
                    type="warning"
                    message="No hay asientos disponibles para este segmento. Por favor, seleccione otro tramo."
                  />
                </Card>
              ) : (
                <>
                  <Card className="seat-selection-card">
                    <h3>Mapa de Asientos</h3>
                    <p className="available-count">
                      {availableSeats.length} disponibles de {allSeats.length} asientos totales
                    </p>
                    <div className="seats-grid">
                      {allSeats.map((seat) => (
                        <button
                          type="button"
                          key={seat.seatId}
                          className={`seat-btn ${
                            seat.isOccupied ? 'occupied' :
                            seat.isHeld ? 'held' :
                            selectedSeat?.seatId === seat.seatId ? 'selected' :
                            'available'
                          }`}
                          onClick={() => {
                            // Solo permitir seleccionar asientos disponibles
                            if (seat.available && !seat.isOccupied && !seat.isHeld) {
                              setSelectedSeat(seat)
                            }
                          }}
                          disabled={seat.isOccupied || seat.isHeld}
                          title={
                            seat.isOccupied ? 'Ocupado' :
                            seat.isHeld ? 'En reserva temporal' :
                            seat.available ? 'Disponible' : 'No disponible'
                          }
                        >
                          {seat.seatNumber}
                        </button>
                      ))}
                    </div>
                    <div className="seat-legend">
                      <div className="legend-item">
                        <div className="legend-box available"></div>
                        <span>Disponible</span>
                      </div>
                      <div className="legend-item">
                        <div className="legend-box held"></div>
                        <span>En reserva</span>
                      </div>
                      <div className="legend-item">
                        <div className="legend-box occupied"></div>
                        <span>Ocupado</span>
                      </div>
                      <div className="legend-item">
                        <div className="legend-box selected"></div>
                        <span>Seleccionado</span>
                      </div>
                    </div>
                  </Card>

                  {/* Mostrar precio solo después de seleccionar asiento */}
                  {selectedSeat && calculatedPrice !== null && (
                    <Card className="price-display-card">
                      <div className="price-display">
                        <FaDollarSign className="price-icon" />
                        <div className="price-content">
                          <span className="price-label">Asiento {selectedSeat.seatNumber} - Precio del viaje:</span>
                          <span className="price-amount">
                            {loadingPrice ? 'Calculando...' : `${calculatedPrice.toLocaleString()} COP`}
                          </span>
                        </div>
                      </div>
                      <p className="price-detail">
                        Tramo: {selectedFromStop.name} → {selectedToStop.name}
                      </p>
                    </Card>
                  )}

                  {selectedSeat && (
                    <>
                      <Card className="payment-selection-card">
                        <h3>Método de Pago</h3>
                        <Select
                          label="Seleccione método de pago"
                          value={paymentMethod}
                          onChange={(e) => setPaymentMethod(e.target.value)}
                          options={[
                            { value: 'CASH', label: 'Efectivo' },
                            { value: 'CARD', label: 'Tarjeta' },
                            { value: 'TRANSFER', label: 'Transferencia' },
                            { value: 'QR', label: 'Código QR' },
                          ]}
                          icon={<FaDollarSign />}
                        />
                      </Card>

                      <Button
                        onClick={handleBookTicket}
                        loading={loading}
                        fullWidth
                        disabled={!selectedSeat || !selectedFromStop || !selectedToStop}
                      >
                        Reservar Boleto
                      </Button>
                    </>
                  )}
                </>
              )}
            </div>
          </div>
          )}
        </div>
      )}
    </div>
  )
}

export default TripSearch
