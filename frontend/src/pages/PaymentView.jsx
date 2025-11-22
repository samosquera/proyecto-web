import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Loading from '../components/Loading'
import Alert from '../components/Alert'
import ticketService from '../services/ticketService'
import { FaCreditCard, FaMoneyBillWave, FaExchangeAlt, FaQrcode, FaCheckCircle } from 'react-icons/fa'
import '../styles/Payment.css'

const PaymentView = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { ticketId, paymentMethod, ticketData } = location.state || {}

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [validating, setValidating] = useState(false)

  // Estados para tarjeta de crédito
  const [cardData, setCardData] = useState({
    cardNumber: '',
    cardHolder: '',
    expiryDate: '',
    cvv: '',
  })
  const [cardErrors, setCardErrors] = useState({})

  // Estado para transferencia
  const [referenceNumber, setReferenceNumber] = useState('')

  useEffect(() => {
    if (!ticketId || !paymentMethod) {
      setError('Información de pago incompleta')
      setTimeout(() => navigate('/'), 3000)
    }
  }, [ticketId, paymentMethod, navigate])

  const validateCardNumber = (number) => {
    const cleaned = number.replace(/\s/g, '')
    return cleaned.length >= 13 && cleaned.length <= 19 && /^\d+$/.test(cleaned)
  }

  const validateExpiryDate = (date) => {
    const regex = /^(0[1-9]|1[0-2])\/\d{2}$/
    if (!regex.test(date)) return false

    const [month, year] = date.split('/')
    const expiry = new Date(2000 + parseInt(year), parseInt(month) - 1)
    const now = new Date()

    return expiry > now
  }

  const validateCVV = (cvv) => {
    return /^\d{3,4}$/.test(cvv)
  }

  const handleCardInputChange = (e) => {
    const { name, value } = e.target
    let formattedValue = value

    if (name === 'cardNumber') {
      formattedValue = value.replace(/\s/g, '').replace(/(\d{4})/g, '$1 ').trim()
    } else if (name === 'expiryDate') {
      formattedValue = value.replace(/\D/g, '')
      if (formattedValue.length >= 2) {
        formattedValue = formattedValue.slice(0, 2) + '/' + formattedValue.slice(2, 4)
      }
    } else if (name === 'cvv') {
      formattedValue = value.replace(/\D/g, '').slice(0, 4)
    }

    setCardData((prev) => ({ ...prev, [name]: formattedValue }))
    setCardErrors((prev) => ({ ...prev, [name]: '' }))
  }

  const validateCardForm = () => {
    const errors = {}

    if (!cardData.cardNumber || !validateCardNumber(cardData.cardNumber)) {
      errors.cardNumber = 'Número de tarjeta inválido (13-19 dígitos)'
    }

    if (!cardData.cardHolder || cardData.cardHolder.trim().length < 3) {
      errors.cardHolder = 'Nombre del titular requerido'
    }

    if (!cardData.expiryDate || !validateExpiryDate(cardData.expiryDate)) {
      errors.expiryDate = 'Fecha de expiración inválida o vencida (MM/YY)'
    }

    if (!cardData.cvv || !validateCVV(cardData.cvv)) {
      errors.cvv = 'CVV inválido (3-4 dígitos)'
    }

    setCardErrors(errors)
    return Object.keys(errors).length === 0
  }

  const simulatePaymentValidation = async () => {
    setValidating(true)
    // Simular validación de 2-3 segundos
    await new Promise((resolve) => setTimeout(resolve, 2500))
    setValidating(false)
  }

  const handleConfirmPayment = async () => {
    setError('')

    // Validaciones específicas por método
    if (paymentMethod === 'CARD') {
      if (!validateCardForm()) {
        setError('Por favor complete correctamente todos los datos de la tarjeta')
        return
      }
      // Simular validación de la pasarela
      await simulatePaymentValidation()
    } else if (paymentMethod === 'TRANSFER') {
      if (!referenceNumber || referenceNumber.trim().length < 6) {
        setError('Por favor ingrese un número de referencia válido (mínimo 6 caracteres)')
        return
      }
      // Simular validación de transferencia
      await simulatePaymentValidation()
    }

    try {
      setLoading(true)
      await ticketService.confirmPayment(ticketId, { paymentMethod })
      setSuccess('Boleto confirmado exitosamente. Redirigiendo a "Mis Boletos"...')
      setTimeout(() => navigate('/my-tickets'), 2000)
    } catch (err) {
      setError(err.response?.data?.message || 'Error al confirmar el pago del boleto')
    } finally {
      setLoading(false)
    }
  }

  const renderPaymentMethodIcon = () => {
    const icons = {
      CARD: <FaCreditCard size={48} />,
      CASH: <FaMoneyBillWave size={48} />,
      TRANSFER: <FaExchangeAlt size={48} />,
      QR: <FaQrcode size={48} />,
    }
    return icons[paymentMethod] || null
  }

  const renderPaymentForm = () => {
    switch (paymentMethod) {
      case 'CARD':
        return (
          <div className="payment-form">
            <h3>Pasarela de Pago - Tarjeta de Crédito/Débito</h3>
            <p className="payment-subtitle">Ingrese los datos de su tarjeta de forma segura</p>

            <div className="card-form">
              <Input
                label="Número de Tarjeta"
                name="cardNumber"
                value={cardData.cardNumber}
                onChange={handleCardInputChange}
                placeholder="1234 5678 9012 3456"
                maxLength="19"
                error={cardErrors.cardNumber}
                icon={<FaCreditCard />}
              />

              <Input
                label="Nombre del Titular"
                name="cardHolder"
                value={cardData.cardHolder}
                onChange={handleCardInputChange}
                placeholder="JUAN PEREZ"
                error={cardErrors.cardHolder}
              />

              <div className="card-row">
                <Input
                  label="Fecha de Expiración"
                  name="expiryDate"
                  value={cardData.expiryDate}
                  onChange={handleCardInputChange}
                  placeholder="MM/YY"
                  maxLength="5"
                  error={cardErrors.expiryDate}
                />

                <Input
                  label="CVV"
                  name="cvv"
                  type="password"
                  value={cardData.cvv}
                  onChange={handleCardInputChange}
                  placeholder="123"
                  maxLength="4"
                  error={cardErrors.cvv}
                />
              </div>

              {validating && (
                <div className="validation-message">
                  <Loading message="Validando datos de la tarjeta..." />
                </div>
              )}
            </div>
          </div>
        )

      case 'CASH':
        return (
          <div className="payment-instructions">
            <h3>Pago en Efectivo</h3>
            <div className="instructions-card">
              <FaMoneyBillWave size={64} color="#f59e0b" />
              <h4>Instrucciones para completar su pago</h4>
              <ol>
                <li>Diríjase a cualquiera de nuestras oficinas o puntos de venta autorizados</li>
                <li>Presente el código de su boleto: <strong>{ticketId}</strong></li>
                <li>Realice el pago en efectivo del monto total</li>
                <li>Solicite su comprobante de pago</li>
                <li>Su boleto será activado inmediatamente después del pago</li>
              </ol>
              <Alert
                type="warning"
                message="Su boleto permanecerá en estado PENDIENTE hasta que complete el pago en persona. Tiene 24 horas para confirmar su reserva."
              />
            </div>
          </div>
        )

      case 'TRANSFER':
        return (
          <div className="payment-instructions">
            <h3>Pago por Transferencia Bancaria</h3>
            <div className="instructions-card">
              <FaExchangeAlt size={64} color="#3b82f6" />
              <h4>Datos para la transferencia</h4>
              <div className="bank-details">
                <p><strong>Banco:</strong> Banco Nacional</p>
                <p><strong>Cuenta:</strong> 1234-5678-9012-3456</p>
                <p><strong>Titular:</strong> BERS Transport S.A.</p>
                <p><strong>Concepto:</strong> Ticket #{ticketId}</p>
              </div>

              <div className="reference-input">
                <Input
                  label="Número de Referencia de Transferencia"
                  value={referenceNumber}
                  onChange={(e) => setReferenceNumber(e.target.value)}
                  placeholder="Ej: REF123456789"
                  helperText="Ingrese el número de referencia que le proporcionó su banco"
                  icon={<FaExchangeAlt />}
                />
              </div>

              {validating && (
                <div className="validation-message">
                  <Loading message="Validando transferencia..." />
                </div>
              )}

              <Alert
                type="info"
                message="Una vez ingrese el número de referencia y confirme, validaremos su transferencia. Recibirá una notificación cuando su pago sea aprobado."
              />
            </div>
          </div>
        )

      case 'QR':
        return (
          <div className="payment-instructions">
            <h3>Pago con Código QR</h3>
            <div className="instructions-card">
              <FaQrcode size={64} color="#8b5cf6" />
              <h4>Instrucciones para pagar con QR</h4>
              <ol>
                <li>Abra la aplicación de su banco móvil</li>
                <li>Seleccione la opción de pago por QR</li>
                <li>Escanee el siguiente código QR en nuestras oficinas</li>
                <li>Verifique el monto y confirme el pago</li>
                <li>Guarde el comprobante de transacción</li>
              </ol>
              <Alert
                type="warning"
                message="Su boleto permanecerá en estado PENDIENTE hasta que complete el pago. Diríjase a nuestras oficinas para escanear el código QR. Tiene 24 horas para confirmar su reserva."
              />
            </div>
          </div>
        )

      default:
        return null
    }
  }

  if (!ticketId || !paymentMethod) {
    return (
      <div className="payment-view">
        <Card>
          <Alert type="error" message="Información de pago no disponible. Redirigiendo..." />
        </Card>
      </div>
    )
  }

  return (
    <div className="payment-view">
      <div className="payment-header">
        <div className="payment-icon">{renderPaymentMethodIcon()}</div>
        <h1>Confirmar Pago del Boleto</h1>
        <p className="payment-subtitle">
          Código de Boleto: <strong>#{ticketId}</strong>
        </p>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} />}

      <Card className="payment-card">
        {renderPaymentForm()}

        <div className="payment-actions">
          <Button variant="secondary" onClick={() => navigate('/')} disabled={loading || validating}>
            Cancelar
          </Button>
          <Button
            onClick={handleConfirmPayment}
            loading={loading || validating}
            disabled={loading || validating}
            icon={<FaCheckCircle />}
          >
            Confirmar Ticket
          </Button>
        </div>
      </Card>

      {ticketData && (
        <Card className="ticket-summary">
          <h3>Resumen del Boleto</h3>
          <div className="summary-details">
            <p><strong>Ruta:</strong> {ticketData.origin} → {ticketData.destination}</p>
            <p><strong>Fecha:</strong> {ticketData.departureAt}</p>
            <p><strong>Asiento:</strong> {ticketData.seatNumber}</p>
            <p><strong>Método de Pago:</strong> {getPaymentMethodLabel(paymentMethod)}</p>
          </div>
        </Card>
      )}
    </div>
  )
}

const getPaymentMethodLabel = (method) => {
  const labels = {
    CARD: 'Tarjeta de Crédito/Débito',
    CASH: 'Efectivo',
    TRANSFER: 'Transferencia Bancaria',
    QR: 'Código QR',
  }
  return labels[method] || method
}

export default PaymentView
