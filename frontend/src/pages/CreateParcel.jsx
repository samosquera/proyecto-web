import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Select from '../components/Select'
import Alert from '../components/Alert'
import parcelService from '../services/parcelService'
import routeService from '../services/routeService'
import tripService from '../services/tripService'
import { FaBox, FaUser, FaPhone, FaDollarSign, FaMapMarkerAlt, FaInfoCircle } from 'react-icons/fa'
import '../styles/CreateParcel.css'

const CreateParcel = () => {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [routes, setRoutes] = useState([])
  const [selectedRoute, setSelectedRoute] = useState(null)
  const [stops, setStops] = useState([])
  const [trips, setTrips] = useState([])
  const [showLoginPrompt, setShowLoginPrompt] = useState(false)

  const [formData, setFormData] = useState({
    senderName: '',
    senderPhone: '',
    receiverName: '',
    receiverPhone: '',
    price: '',
    fromStopId: '',
    toStopId: '',
    tripId: '',
  })

  useEffect(() => {
    loadRoutes()
  }, [])

  const loadRoutes = async () => {
    try {
      const data = await routeService.getAllRoutes()
      setRoutes(data)
    } catch (err) {
      setError('Error al cargar rutas')
    }
  }

  const handleRouteSelect = async (routeId) => {
    try {
      setLoading(true)
      const route = routes.find((r) => r.id === parseInt(routeId))
      setSelectedRoute(route)

      // Cargar paradas de la ruta
      const stopsData = await routeService.getRouteStops(routeId)
      setStops(stopsData)

      // Cargar viajes activos de esta ruta
      const today = new Date().toISOString().split('T')[0]
      const tripsData = await tripService.getTripsByRoute(routeId, today)
      setTrips(tripsData.filter((t) => t.status === 'SCHEDULED' || t.status === 'BOARDING'))
    } catch (err) {
      setError('Error al cargar información de la ruta')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))

    if (name === 'routeId') {
      handleRouteSelect(value)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    // Verificar si el usuario está autenticado
    if (!isAuthenticated()) {
      setShowLoginPrompt(true)
      setError('Debes iniciar sesión para finalizar el registro de la encomienda')
      return
    }

    // Validaciones
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

    try {
      setLoading(true)
      const parcelData = {
        ...formData,
        fromStopId: parseInt(formData.fromStopId),
        toStopId: parseInt(formData.toStopId),
        tripId: formData.tripId ? parseInt(formData.tripId) : null,
        price: parseFloat(formData.price),
      }

      const created = await parcelService.createParcel(parcelData)
      setSuccess(
        `¡Encomienda creada exitosamente! Código de seguimiento: ${created.code}. Código OTP de entrega: ${created.deliveryOtp}. Por favor, guarde estos códigos de forma segura.`
      )

      // Resetear formulario después de mostrar el mensaje
      setTimeout(() => {
        setFormData({
          senderName: '',
          senderPhone: '',
          receiverName: '',
          receiverPhone: '',
          price: '',
          fromStopId: '',
          toStopId: '',
          tripId: '',
        })
        setSelectedRoute(null)
        setStops([])
        setTrips([])
        setSuccess('')
      }, 10000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear encomienda')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="create-parcel-page">
      <div className="page-header">
        <FaBox className="page-icon" />
        <h1>Registrar Encomienda</h1>
        <p>Complete el formulario para enviar una encomienda</p>
      </div>

      {!isAuthenticated() && (
        <Alert
          type="info"
          message="Puedes completar el formulario, pero deberás iniciar sesión antes de finalizar el registro de la encomienda."
        />
      )}

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} />}

      <Card className="create-parcel-form-card">
        <form onSubmit={handleSubmit} className="parcel-form">
          <div className="form-section">
            <h3>
              <FaUser /> Información del Remitente
            </h3>
            <div className="form-grid">
              <Input
                label="Nombre del Remitente"
                type="text"
                name="senderName"
                value={formData.senderName}
                onChange={handleInputChange}
                required
                icon={<FaUser />}
                placeholder="Juan Pérez"
              />
              <Input
                label="Teléfono del Remitente"
                type="tel"
                name="senderPhone"
                value={formData.senderPhone}
                onChange={handleInputChange}
                required
                icon={<FaPhone />}
                placeholder="3001234567"
                maxLength={10}
                pattern="\d{10}"
                title="Ingrese exactamente 10 dígitos"
              />
            </div>
          </div>

          <div className="form-section">
            <h3>
              <FaUser /> Información del Destinatario
            </h3>
            <div className="form-grid">
              <Input
                label="Nombre del Destinatario"
                type="text"
                name="receiverName"
                value={formData.receiverName}
                onChange={handleInputChange}
                required
                icon={<FaUser />}
                placeholder="María García"
              />
              <Input
                label="Teléfono del Destinatario"
                type="tel"
                name="receiverPhone"
                value={formData.receiverPhone}
                onChange={handleInputChange}
                required
                icon={<FaPhone />}
                placeholder="3009876543"
                maxLength={10}
                pattern="\d{10}"
                title="Ingrese exactamente 10 dígitos"
              />
            </div>
          </div>

          <div className="form-section">
            <h3>
              <FaBox /> Detalles de la Encomienda
            </h3>
            <div className="form-grid">
              <Input
                label="Precio (Cop)"
                type="number"
                name="price"
                value={formData.price}
                onChange={handleInputChange}
                required
                min="0"
                step="0.01"
                icon={<FaDollarSign />}
                placeholder="0.00"
              />
              <Select
                label="Ruta"
                name="routeId"
                value={formData.routeId}
                onChange={handleInputChange}
                options={routes.map((r) => ({
                  value: r.id,
                  label: `${r.origin} → ${r.destination}`,
                }))}
                placeholder="Seleccione una ruta"
                required
                icon={<FaMapMarkerAlt />}
              />
            </div>

            {selectedRoute && (
              <>
                <div className="form-grid">
                  <Select
                    label="Parada de Origen"
                    name="fromStopId"
                    value={formData.fromStopId}
                    onChange={handleInputChange}
                    options={stops.map((s) => ({
                      value: s.id,
                      label: `${s.name} (Parada ${s.order})`,
                    }))}
                    placeholder="Seleccione parada de origen"
                    required
                    icon={<FaMapMarkerAlt />}
                  />
                  <Select
                    label="Parada de Destino"
                    name="toStopId"
                    value={formData.toStopId}
                    onChange={handleInputChange}
                    options={stops
                      .filter(
                        (s) =>
                          !formData.fromStopId ||
                          s.order >
                            stops.find((stop) => stop.id === parseInt(formData.fromStopId))
                              ?.order
                      )
                      .map((s) => ({
                        value: s.id,
                        label: `${s.name} (Parada ${s.order})`,
                      }))}
                    placeholder="Seleccione parada de destino"
                    required
                    disabled={!formData.fromStopId}
                    icon={<FaMapMarkerAlt />}
                  />
                </div>

                <Select
                  label="Viaje Específico (Opcional)"
                  name="tripId"
                  value={formData.tripId}
                  onChange={handleInputChange}
                  options={trips.map((t) => ({
                    value: t.id,
                    label: `${t.date} - ${t.departureAt} | Bus: ${t.bus?.plate}`,
                  }))}
                  placeholder="No asignar a viaje específico"
                  icon={<FaBox />}
                />
              </>
            )}
          </div>

          <div className="info-box">
            <FaInfoCircle />
            <div>
              <strong>Información importante:</strong>
              <ul>
                <li>Al crear la encomienda recibirás un código de seguimiento único.</li>
                <li>Se generará un código OTP de 6 dígitos necesario para la entrega.</li>
                <li>
                  Por favor, comunica el código OTP al destinatario para que pueda retirar la
                  encomienda.
                </li>
                <li>Guarda ambos códigos en un lugar seguro.</li>
              </ul>
            </div>
          </div>

          <Button
            type="submit"
            fullWidth
            loading={loading}
            disabled={loading}
            className="submit-btn"
          >
            {isAuthenticated() ? 'Registrar Encomienda' : 'Continuar (Requiere Login)'}
          </Button>
        </form>
      </Card>
    </div>
  )
}

export default CreateParcel
