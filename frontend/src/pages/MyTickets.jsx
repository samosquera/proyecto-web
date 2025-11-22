import React, { useState, useEffect } from 'react'
import Card from '../components/Card'
import Button from '../components/Button'
import Loading from '../components/Loading'
import Alert from '../components/Alert'
import ticketService from '../services/ticketService'
import { FaTicketAlt, FaBus, FaMapMarkerAlt, FaClock, FaQrcode } from 'react-icons/fa'
import { format } from 'date-fns'
import '../styles/MyTickets.css'

const MyTickets = () => {
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [selectedTicketForPayment, setSelectedTicketForPayment] = useState(null)
  const [paymentData, setPaymentData] = useState({
    paymentMethod: 'CASH',
    reference: '',
    processing: false,
  })

  useEffect(() => {
    loadTickets()
  }, [])

  const loadTickets = async () => {
    try {
      setLoading(true)
      const data = await ticketService.getMyTickets()
      setTickets(data)
    } catch (err) {
      setError('Error al cargar boletos')
    } finally {
      setLoading(false)
    }
  }

  const handleCancelTicket = async (ticketId) => {
    if (!window.confirm('¿Está seguro de cancelar este boleto?')) {
      return
    }

    try {
      await ticketService.cancelTicket(ticketId, 'Cancelado por usuario')
      setSuccess('Boleto cancelado exitosamente')
      loadTickets()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cancelar boleto')
    }
  }

  const handleOpenPaymentModal = (ticket) => {
    setSelectedTicketForPayment(ticket)
    setShowPaymentModal(true)
    setPaymentData({
      paymentMethod: ticket.paymentMethod || 'CASH',
      reference: '',
      processing: false,
    })
  }

  const handleClosePaymentModal = () => {
    setShowPaymentModal(false)
    setSelectedTicketForPayment(null)
    setPaymentData({
      paymentMethod: 'CASH',
      reference: '',
      processing: false,
    })
  }

  const handleConfirmPayment = async () => {
    if (
      ['CARD', 'TRANSFER', 'QR'].includes(paymentData.paymentMethod) &&
      !paymentData.reference
    ) {
      setError('Por favor ingrese la referencia del pago')
      return
    }

    try {
      setPaymentData((prev) => ({ ...prev, processing: true }))

      // Simular procesamiento del pago
      await new Promise((resolve) => setTimeout(resolve, 1500))

      await ticketService.confirmPayment(selectedTicketForPayment.id, {
        paymentMethod: paymentData.paymentMethod,
      })

      setSuccess(
        `Pago confirmado exitosamente con ${getPaymentMethodLabel(paymentData.paymentMethod)}`
      )
      handleClosePaymentModal()
      loadTickets()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al confirmar pago')
      setPaymentData((prev) => ({ ...prev, processing: false }))
    }
  }

  const getPaymentMethodLabel = (method) => {
    const labels = {
      CASH: 'Efectivo',
      CARD: 'Tarjeta',
      TRANSFER: 'Transferencia',
      QR: 'Código QR',
    }
    return labels[method] || method
  }

  const getStatusLabel = (status) => {
    const labels = {
      PENDING_PAYMENT: 'Pago Pendiente',
      SOLD: 'Confirmado',
      CANCELLED: 'Cancelado',
      NO_SHOW: 'No presentado',
      USED: 'Usado',
    }
    return labels[status] || status
  }

  const getStatusClass = (status) => {
    const classes = {
      PENDING_PAYMENT: 'status-pending',
      SOLD: 'status-confirmed',
      CANCELLED: 'status-cancelled',
      NO_SHOW: 'status-no-show',
      USED: 'status-used',
    }
    return classes[status] || ''
  }

  if (loading) {
    return <Loading message="Cargando boletos..." />
  }

  return (
    <div className="my-tickets">
      <h1>Mis Boletos</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {tickets.length === 0 ? (
        <Card>
          <div className="no-tickets">
            <FaTicketAlt size={64} />
            <p>No tienes boletos registrados</p>
            <Button onClick={() => (window.location.href = '/search-trips')}>
              Buscar Viajes
            </Button>
          </div>
        </Card>
      ) : (
        <div className="tickets-grid">
          {tickets.map((ticket) => (
            <Card key={ticket.id} className="ticket-card">
              <div className="ticket-header">
                <FaTicketAlt className="ticket-icon" />
                <span className={`ticket-status ${getStatusClass(ticket.status)}`}>
                  {getStatusLabel(ticket.status)}
                </span>
              </div>

              <div className="ticket-body">
                <div className="ticket-info">
                  <div className="info-row route-info">
                    <FaMapMarkerAlt />
                    <span className="route-text">
                      <strong>{ticket.routeOrigin} → {ticket.routeDestination}</strong>
                    </span>
                  </div>
                  <div className="info-row">
                    <span className="segment-info">
                      Tramo: {ticket.fromStopName} → {ticket.toStopName}
                    </span>
                  </div>
                  <div className="info-row">
                    <FaBus />
                    <span>Bus: <strong>{ticket.busPlate || 'N/A'}</strong></span>
                  </div>
                  <div className="info-row">
                    <FaClock />
                    <span>
                      Salida: <strong>{ticket.tripDate} a las {ticket.tripTime}</strong>
                    </span>
                  </div>
                  <div className="info-row seat-price">
                    <span>Asiento: <strong>{ticket.seatNumber}</strong></span>
                    <span className="price-tag">${ticket.price.toLocaleString()} COP</span>
                  </div>
                  {ticket.qrCode && (
                    <div className="info-row qr-code">
                      <FaQrcode />
                      <span className="qr-text">{ticket.qrCode}</span>
                    </div>
                  )}
                </div>

                <div className="ticket-actions">
                  {ticket.status === 'PENDING_PAYMENT' && (
                    <>
                      <Button
                        onClick={() => handleOpenPaymentModal(ticket)}
                        variant="primary"
                        size="small"
                      >
                        Confirmar Pago
                      </Button>
                      <Button
                        onClick={() => handleCancelTicket(ticket.id)}
                        variant="danger"
                        size="small"
                      >
                        Cancelar
                      </Button>
                    </>
                  )}

                  {ticket.status === 'SOLD' && (
                    <Button
                      onClick={() => handleCancelTicket(ticket.id)}
                      variant="danger"
                      size="small"
                    >
                      Cancelar Boleto
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Modal de Confirmación de Pago */}
      {showPaymentModal && selectedTicketForPayment && (
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
          onClick={handleClosePaymentModal}
        >
          <div
            style={{
              backgroundColor: 'white',
              borderRadius: '12px',
              padding: '24px',
              maxWidth: '500px',
              width: '90%',
              maxHeight: '90vh',
              overflow: 'auto',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginBottom: '16px' }}>Confirmar Pago del Boleto</h2>

            <Card style={{ marginBottom: '16px', backgroundColor: '#f8f9fa' }}>
              <div>
                <strong>Viaje:</strong> {selectedTicketForPayment.routeOrigin} →{' '}
                {selectedTicketForPayment.routeDestination}
              </div>
              <div>
                <strong>Tramo:</strong> {selectedTicketForPayment.fromStopName} →{' '}
                {selectedTicketForPayment.toStopName}
              </div>
              <div>
                <strong>Asiento:</strong> {selectedTicketForPayment.seatNumber}
              </div>
              <div style={{ fontSize: '1.2rem', marginTop: '8px', color: '#2ecc71' }}>
                <strong>Total a pagar: ${selectedTicketForPayment.price.toLocaleString()} COP</strong>
              </div>
            </Card>

            <Select
              label="Método de Pago"
              value={paymentData.paymentMethod}
              onChange={(e) =>
                setPaymentData((prev) => ({ ...prev, paymentMethod: e.target.value }))
              }
              options={[
                { value: 'CASH', label: 'Efectivo' },
                { value: 'CARD', label: 'Tarjeta de Crédito/Débito' },
                { value: 'TRANSFER', label: 'Transferencia Bancaria' },
                { value: 'QR', label: 'Código QR' },
              ]}
            />

            {['CARD', 'TRANSFER', 'QR'].includes(paymentData.paymentMethod) && (
              <Input
                label={
                  paymentData.paymentMethod === 'CARD'
                    ? 'Número de Tarjeta (Mock)'
                    : paymentData.paymentMethod === 'TRANSFER'
                    ? 'Referencia de Transferencia'
                    : 'Código de Transacción'
                }
                type="text"
                value={paymentData.reference}
                onChange={(e) =>
                  setPaymentData((prev) => ({ ...prev, reference: e.target.value }))
                }
                placeholder={
                  paymentData.paymentMethod === 'CARD'
                    ? '1234-5678-9012-3456'
                    : 'Ingrese referencia'
                }
              />
            )}

            <div
              style={{
                marginTop: '16px',
                padding: '12px',
                backgroundColor: '#e3f2fd',
                borderRadius: '8px',
                fontSize: '0.9rem',
              }}
            >
              <strong>Nota:</strong> Este es un sistema de pagos simulado (mock). En un entorno
              real, se integraría con una pasarela de pagos.
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
              <Button
                onClick={handleConfirmPayment}
                variant="success"
                fullWidth
                loading={paymentData.processing}
                disabled={paymentData.processing}
              >
                {paymentData.processing ? 'Procesando...' : 'Confirmar Pago'}
              </Button>
              <Button
                onClick={handleClosePaymentModal}
                variant="secondary"
                fullWidth
                disabled={paymentData.processing}
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

export default MyTickets
