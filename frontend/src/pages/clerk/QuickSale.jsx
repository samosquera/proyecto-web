import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import tripService from '../../services/tripService'
import quickSaleService from '../../services/quickSaleService'
import userService from '../../services/userService'
import routeService from '../../services/routeService'
import {
  FaTicketAlt,
  FaBus,
  FaMapMarkerAlt,
  FaClock,
  FaUser,
  FaPhone,
  FaDollarSign,
  FaCheckCircle,
  FaSearch,
} from 'react-icons/fa'
import '../../styles/QuickSale.css'

const QuickSale = () => {
  const [trips, setTrips] = useState([])
  const [selectedTrip, setSelectedTrip] = useState(null)
  const [availableSeats, setAvailableSeats] = useState([])
  const [stops, setStops] = useState([])
  const [passenger, setPassenger] = useState(null)
  const [loading, setLoading] = useState(false)
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [phoneSearch, setPhoneSearch] = useState('')
  const [formData, setFormData] = useState({
    seatNumber: '',
    fromStopId: '',
    toStopId: '',
    paymentMethod: 'CASH',
    applyDiscount: false,
  })

  useEffect(() => {
    loadTodayTrips()
  }, [])

  const loadTodayTrips = async () => {
    try {
      setLoading(true)
      const data = await tripService.getTodayActiveTrips()
      setTrips(data)
    } catch (err) {
      setError('Error al cargar viajes del día')
    } finally {
      setLoading(false)
    }
  }

  const handleSelectTrip = async (trip) => {
    setSelectedTrip(trip)
    setFormData({ ...formData, seatNumber: '', fromStopId: '', toStopId: '' })
    setStops([])

    try {
      // Cargar asientos disponibles
      const seatsData = await quickSaleService.getAvailableSeats(trip.id)
      setAvailableSeats(seatsData.availableSeats || [])

      // Cargar paradas de la ruta del viaje
      const stopsData = await routeService.getRouteStops(trip.routeId)
      setStops(stopsData)
    } catch (err) {
      setError('Error al cargar información del viaje')
      setAvailableSeats([])
      setStops([])
    }
  }

  const handleSearchPassenger = async () => {
    if (!phoneSearch.trim()) {
      setError('Por favor ingrese un número de teléfono')
      return
    }

    try {
      setSearching(true)
      setError('')
      const userData = await userService.getUserByPhone(phoneSearch)

      // Verificar que sea un pasajero
      if (userData.role !== 'PASSENGER') {
        setError('El usuario encontrado no es un pasajero')
        setPassenger(null)
        return
      }

      setPassenger(userData)
      setSuccess(`Pasajero encontrado: ${userData.firstName} ${userData.lastName}`)
    } catch (err) {
      if (err.response?.status === 404) {
        setError('No se encontró un pasajero con ese número de teléfono. Por favor registre al pasajero primero.')
      } else {
        setError('Error al buscar pasajero')
      }
      setPassenger(null)
    } finally {
      setSearching(false)
    }
  }

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    // Validaciones
    if (!passenger) {
      setError('Debe buscar y seleccionar un pasajero primero')
      return
    }

    if (!selectedTrip) {
      setError('Debe seleccionar un viaje')
      return
    }

    if (!formData.fromStopId || !formData.toStopId) {
      setError('Debe seleccionar paradas de origen y destino')
      return
    }

    const fromStop = stops.find((s) => s.id === parseInt(formData.fromStopId))
    const toStop = stops.find((s) => s.id === parseInt(formData.toStopId))

    if (fromStop.order >= toStop.order) {
      setError('La parada de destino debe ser posterior a la de origen')
      return
    }

    if (!formData.seatNumber) {
      setError('Debe seleccionar un asiento')
      return
    }

    try {
      setLoading(true)

      const saleData = {
        tripId: selectedTrip.id,
        passengerId: passenger.id,
        seatNumber: formData.seatNumber, // String, not integer
        fromStopId: parseInt(formData.fromStopId),
        toStopId: parseInt(formData.toStopId),
        paymentMethod: formData.paymentMethod,
        applyDiscount: formData.applyDiscount,
      }

      const result = await quickSaleService.createQuickSale(saleData)
      setSuccess(
        `¡Venta realizada exitosamente! Asiento ${result.seatNumber} - Pasajero: ${passenger.firstName} ${passenger.lastName}. Precio final: $ ${result.finalPrice?.toLocaleString('es-CO')} COP`
      )

      // Resetear formulario
      setFormData({
        seatNumber: '',
        fromStopId: '',
        toStopId: '',
        paymentMethod: 'CASH',
        applyDiscount: false,
      })
      setPassenger(null)
      setPhoneSearch('')

      // Recargar asientos disponibles
      if (selectedTrip) {
        handleSelectTrip(selectedTrip)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al realizar la venta')
    } finally {
      setLoading(false)
    }
  }

  if (loading && trips.length === 0) {
    return <Loading message="Cargando viajes disponibles..." />
  }

  return (
    <div className="quick-sale">
      <div className="page-header">
        <h1>Venta Rápida de Boletos</h1>
        <p>Vender boletos para viajes programados del día</p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {/* Búsqueda de pasajero por teléfono */}
      <Card className="passenger-search-card">
        <h3>Buscar Pasajero</h3>
        <div className="search-form">
          <Input
            label="Número de Teléfono"
            name="phoneSearch"
            value={phoneSearch}
            onChange={(e) => setPhoneSearch(e.target.value)}
            icon={<FaPhone />}
            placeholder="3001234567"
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault()
                handleSearchPassenger()
              }
            }}
          />
          <Button
            type="button"
            onClick={handleSearchPassenger}
            loading={searching}
            icon={<FaSearch />}
          >
            Buscar
          </Button>
        </div>

        {passenger && (
          <div className="passenger-info">
            <FaUser className="passenger-icon" />
            <div className="passenger-details">
              <p className="passenger-name">
                {passenger.firstName} {passenger.lastName}
              </p>
              <p className="passenger-contact">
                Tel: {passenger.phone} | Email: {passenger.email}
              </p>
            </div>
          </div>
        )}
      </Card>

      {/* Selección de viaje */}
      <Card className="trips-selection-card">
        <h3>Seleccionar Viaje</h3>
        {trips.length === 0 ? (
          <p>No hay viajes disponibles para hoy</p>
        ) : (
          <div className="trips-list">
            {trips.map((trip) => (
              <div
                key={trip.id}
                className={`trip-item ${selectedTrip?.id === trip.id ? 'selected' : ''}`}
                onClick={() => handleSelectTrip(trip)}
              >
                <div className="trip-route">
                  <FaMapMarkerAlt />
                  <span>
                    {trip.origin} → {trip.destination}
                  </span>
                </div>
                <div className="trip-details">
                  <div className="trip-detail">
                    <FaBus />
                    <span>{trip.busPlate}</span>
                  </div>
                  <div className="trip-detail">
                    <FaClock />
                    <span>{trip.departureAt}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Formulario de venta */}
      {selectedTrip && passenger && (
        <Card className="sale-form-card">
          <h3>Detalles de la Venta</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-grid">
              <Select
                label="Parada de Origen"
                name="fromStopId"
                value={formData.fromStopId}
                onChange={handleInputChange}
                required
                icon={<FaMapMarkerAlt />}
                options={stops.map((stop) => ({
                  value: stop.id,
                  label: `${stop.order}. ${stop.name}`,
                }))}
                placeholder="Seleccione parada de origen"
              />

              <Select
                label="Parada de Destino"
                name="toStopId"
                value={formData.toStopId}
                onChange={handleInputChange}
                required
                icon={<FaMapMarkerAlt />}
                options={stops.map((stop) => ({
                  value: stop.id,
                  label: `${stop.order}. ${stop.name}`,
                }))}
                placeholder="Seleccione parada de destino"
              />

              <Select
                label="Número de Asiento"
                name="seatNumber"
                value={formData.seatNumber}
                onChange={handleInputChange}
                required
                options={availableSeats.map((seat) => ({
                  value: seat,
                  label: `Asiento ${seat}`,
                }))}
                placeholder="Seleccione un asiento"
              />

              <Select
                label="Método de Pago"
                name="paymentMethod"
                value={formData.paymentMethod}
                onChange={handleInputChange}
                required
                icon={<FaDollarSign />}
                options={[
                  { value: 'CASH', label: 'Efectivo' },
                  { value: 'CARD', label: 'Tarjeta' },
                  { value: 'TRANSFER', label: 'Transferencia' },
                  { value: 'QR', label: 'Código QR' },
                ]}
              />
            </div>

            <div className="form-checkbox">
              <label>
                <input
                  type="checkbox"
                  name="applyDiscount"
                  checked={formData.applyDiscount}
                  onChange={handleInputChange}
                />
                <span>Aplicar descuento</span>
              </label>
            </div>

            <div className="form-info">
              <p>
                <strong>Pasajero:</strong> {passenger.firstName} {passenger.lastName}
              </p>
              <p>
                <strong>Viaje:</strong> {selectedTrip.origin} → {selectedTrip.destination}
              </p>
              <p>
                <strong>Salida:</strong> {selectedTrip.departureAt}
              </p>
              <p>
                <strong>Bus:</strong> {selectedTrip.busPlate}
              </p>
            </div>

            <div className="form-actions">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setSelectedTrip(null)
                  setFormData({
                    seatNumber: '',
                    fromStopId: '',
                    toStopId: '',
                    paymentMethod: 'CASH',
                    applyDiscount: false,
                  })
                }}
              >
                Cancelar
              </Button>
              <Button type="submit" loading={loading} icon={<FaCheckCircle />}>
                Confirmar Venta
              </Button>
            </div>
          </form>
        </Card>
      )}
    </div>
  )
}

export default QuickSale
