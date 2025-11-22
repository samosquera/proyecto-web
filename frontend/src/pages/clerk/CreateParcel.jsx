import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import parcelService from '../../services/parcelService'
import routeService from '../../services/routeService'
import tripService from '../../services/tripService'
import { FaBox, FaUser, FaPhone, FaDollarSign, FaMapMarkerAlt } from 'react-icons/fa'

const CreateParcel = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [routes, setRoutes] = useState([])
  const [selectedRoute, setSelectedRoute] = useState(null)
  const [stops, setStops] = useState([])
  const [trips, setTrips] = useState([])

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

    // Validaciones
    if (!formData.fromStopId || !formData.toStopId) {
      setError('Debe seleccionar paradas de origen y destino')
      return
    }

    const fromStop = stops.find((s) => s.id === parseInt(formData.fromStopId))
    const toStop = stops.find((s) => s.id === parseInt(formData.toStopId))

    if (fromStop.stopOrder >= toStop.stopOrder) {
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
      setSuccess(`Encomienda creada exitosamente. Código: ${created.code}. OTP: ${created.deliveryOtp}`)

      // Resetear formulario después de 3 segundos
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
        setSuccess('')
      }, 5000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear encomienda')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="create-parcel">
      <h1>Crear Encomienda</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h3>Información del Remitente</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
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
              placeholder="1234567890"
              pattern="\d{10}"
            />
          </div>

          <h3>Información del Destinatario</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
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
              placeholder="0987654321"
              pattern="\d{10}"
            />
          </div>

          <h3>Detalles de la Encomienda</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <Input
              label="Precio"
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
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <Select
                  label="Parada de Origen"
                  name="fromStopId"
                  value={formData.fromStopId}
                  onChange={handleInputChange}
                  options={stops.map((s) => ({
                    value: s.id,
                    label: `${s.name} (Parada ${s.stopOrder})`,
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
                        s.stopOrder >
                          stops.find((stop) => stop.id === parseInt(formData.fromStopId))?.stopOrder
                    )
                    .map((s) => ({
                      value: s.id,
                      label: `${s.name} (Parada ${s.stopOrder})`,
                    }))}
                  placeholder="Seleccione parada de destino"
                  required
                  disabled={!formData.fromStopId}
                  icon={<FaMapMarkerAlt />}
                />
              </div>

              <Select
                label="Viaje (Opcional)"
                name="tripId"
                value={formData.tripId}
                onChange={handleInputChange}
                options={trips.map((t) => ({
                  value: t.id,
                  label: `${t.date} - ${t.departureAt} | Bus: ${t.bus?.plate}`,
                }))}
                placeholder="Sin asignar a viaje"
                icon={<FaBox />}
              />
            </>
          )}

          <div
            style={{
              marginTop: '16px',
              padding: '12px',
              backgroundColor: '#e3f2fd',
              borderRadius: '8px',
            }}
          >
            <strong>Nota:</strong> Al crear la encomienda se generará automáticamente un código de
            seguimiento y un OTP de 6 dígitos para la entrega. Asegúrese de comunicar el OTP al
            destinatario.
          </div>

          <Button type="submit" fullWidth loading={loading} disabled={loading}>
            Crear Encomienda
          </Button>
        </form>
      </Card>
    </div>
  )
}

export default CreateParcel
