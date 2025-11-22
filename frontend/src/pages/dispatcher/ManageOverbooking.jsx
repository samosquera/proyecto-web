import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import overbookingService from '../../services/overbookingService'
import { FaCheck, FaTimes, FaClock, FaExclamationTriangle } from 'react-icons/fa'
import '../../styles/ManageOverbooking.css'

const ManageOverbooking = () => {
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [actionModal, setActionModal] = useState({ show: false, type: '', request: null })
  const [actionNotes, setActionNotes] = useState('')

  useEffect(() => {
    loadPendingRequests()
  }, [])

  const loadPendingRequests = async () => {
    try {
      setLoading(true)
      const data = await overbookingService.getPendingRequests()
      setRequests(data)
    } catch (err) {
      setError('Error al cargar solicitudes de overbooking')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (request) => {
    setActionModal({ show: true, type: 'approve', request })
  }

  const handleReject = async (request) => {
    setActionModal({ show: true, type: 'reject', request })
  }

  const confirmAction = async () => {
    try {
      if (actionModal.type === 'approve') {
        await overbookingService.approveOverbooking(actionModal.request.id, actionNotes)
        setSuccess('Solicitud de overbooking aprobada')
      } else {
        if (!actionNotes.trim()) {
          setError('Debe proporcionar una razón para rechazar')
          return
        }
        await overbookingService.rejectOverbooking(actionModal.request.id, actionNotes)
        setSuccess('Solicitud de overbooking rechazada')
      }
      setActionModal({ show: false, type: '', request: null })
      setActionNotes('')
      loadPendingRequests()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al procesar solicitud')
    }
  }

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: { class: 'pending', label: 'Pendiente', icon: <FaClock /> },
      APPROVED: { class: 'approved', label: 'Aprobada', icon: <FaCheck /> },
      REJECTED: { class: 'rejected', label: 'Rechazada', icon: <FaTimes /> },
      EXPIRED: { class: 'expired', label: 'Expirada', icon: <FaClock /> },
    }
    const badge = badges[status] || badges.PENDING
    return (
      <span className={`status-badge ${badge.class}`}>
        {badge.icon} {badge.label}
      </span>
    )
  }

  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-'
    return new Date(dateTime).toLocaleString('es-ES')
  }

  if (loading) {
    return <Loading message="Cargando solicitudes de overbooking..." />
  }

  return (
    <div className="manage-overbooking">
      <h1>Gestión de Overbooking</h1>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      {requests.length === 0 ? (
        <Card>
          <div className="no-requests">
            <FaCheck size={64} />
            <p>No hay solicitudes de overbooking pendientes</p>
          </div>
        </Card>
      ) : (
        <div className="requests-grid">
          {requests.map((request) => (
            <Card key={request.id} className="overbooking-request-card">
              <div className="request-header">
                <div className="request-id">Solicitud #{request.id}</div>
                {getStatusBadge(request.status)}
              </div>

              <div className="request-details">
                <div className="detail-section">
                  <h3>Información del Viaje</h3>
                  <p><strong>Viaje:</strong> {request.tripInfo}</p>
                  <p><strong>Ocupación actual:</strong> {(request.currentOccupancyRate * 100).toFixed(1)}%</p>
                  <p><strong>Tiempo hasta salida:</strong> {request.minutesUntilDeparture} minutos</p>
                </div>

                <div className="detail-section">
                  <h3>Información del Pasajero</h3>
                  <p><strong>Pasajero:</strong> {request.passengerName}</p>
                  <p><strong>Asiento:</strong> {request.seatNumber}</p>
                  <p><strong>Ticket ID:</strong> #{request.ticketId}</p>
                </div>

                <div className="detail-section">
                  <h3>Detalles de la Solicitud</h3>
                  <p><strong>Razón:</strong> {request.reason}</p>
                  <p><strong>Solicitado por:</strong> {request.requestedByName}</p>
                  <p><strong>Fecha de solicitud:</strong> {formatDateTime(request.requestedAt)}</p>
                  {request.expiresAt && (
                    <p className="warning-text">
                      <FaExclamationTriangle /> <strong>Expira:</strong> {formatDateTime(request.expiresAt)}
                    </p>
                  )}
                </div>
              </div>

              {request.status === 'PENDING' && (
                <div className="request-actions">
                  <Button
                    onClick={() => handleApprove(request)}
                    variant="success"
                  >
                    <FaCheck /> Aprobar
                  </Button>
                  <Button
                    onClick={() => handleReject(request)}
                    variant="danger"
                  >
                    <FaTimes /> Rechazar
                  </Button>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      {actionModal.show && (
        <div className="modal-overlay" onClick={() => setActionModal({ show: false, type: '', request: null })}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>
              {actionModal.type === 'approve' ? 'Aprobar' : 'Rechazar'} Solicitud de Overbooking
            </h2>

            <div className="modal-body">
              <p>
                <strong>Viaje:</strong> {actionModal.request?.tripInfo}
              </p>
              <p>
                <strong>Pasajero:</strong> {actionModal.request?.passengerName}
              </p>
              <p>
                <strong>Asiento:</strong> {actionModal.request?.seatNumber}
              </p>

              <div className="form-group">
                <label>
                  {actionModal.type === 'approve' ? 'Notas (opcional)' : 'Razón de rechazo *'}
                </label>
                <textarea
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  placeholder={
                    actionModal.type === 'approve'
                      ? 'Agregar notas adicionales...'
                      : 'Especificar razón del rechazo...'
                  }
                  rows={4}
                  required={actionModal.type === 'reject'}
                />
              </div>
            </div>

            <div className="modal-actions">
              <Button
                onClick={() => {
                  setActionModal({ show: false, type: '', request: null })
                  setActionNotes('')
                }}
                variant="secondary"
              >
                Cancelar
              </Button>
              <Button
                onClick={confirmAction}
                variant={actionModal.type === 'approve' ? 'success' : 'danger'}
              >
                {actionModal.type === 'approve' ? 'Confirmar Aprobación' : 'Confirmar Rechazo'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageOverbooking
