import React from 'react'
import { FaCheckCircle, FaClock, FaTruck, FaTimesCircle, FaBox } from 'react-icons/fa'
import './ParcelTimeline.css'

const ParcelTimeline = ({ status, createdAt, deliveredAt }) => {
  const steps = [
    {
      key: 'CREATED',
      label: 'Registrada',
      icon: <FaBox />,
      description: 'Encomienda registrada en el sistema',
    },
    {
      key: 'IN_TRANSIT',
      label: 'En Tránsito',
      icon: <FaTruck />,
      description: 'Encomienda en camino a su destino',
    },
    {
      key: 'DELIVERED',
      label: 'Entregada',
      icon: <FaCheckCircle />,
      description: 'Encomienda entregada al destinatario',
    },
  ]

  const getStepStatus = (stepKey) => {
    const statusOrder = ['CREATED', 'IN_TRANSIT', 'DELIVERED', 'FAILED']
    const currentIndex = statusOrder.indexOf(status)
    const stepIndex = statusOrder.indexOf(stepKey)

    // Si el estado actual es FAILED, marcar todos como pendientes excepto CREATED
    if (status === 'FAILED') {
      if (stepKey === 'CREATED') return 'completed'
      return 'failed'
    }

    if (stepIndex < currentIndex) return 'completed'
    if (stepIndex === currentIndex) return 'active'
    return 'pending'
  }

  const formatDate = (dateString) => {
    if (!dateString) return null
    const date = new Date(dateString)
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  return (
    <div className="parcel-timeline">
      {/* Timeline para estado FAILED separado */}
      {status === 'FAILED' && (
        <div className="timeline-step failed-step">
          <div className="step-indicator failed">
            <div className="step-icon">
              <FaTimesCircle />
            </div>
          </div>
          <div className="step-content">
            <div className="step-title">Entrega Fallida</div>
            <div className="step-description">
              No se pudo completar la entrega de la encomienda
            </div>
            {deliveredAt && (
              <div className="step-timestamp">{formatDate(deliveredAt)}</div>
            )}
          </div>
        </div>
      )}

      {/* Timeline normal para otros estados */}
      <div className="timeline-steps">
        {steps.map((step, index) => {
          const stepStatus = getStepStatus(step.key)
          const isLast = index === steps.length - 1

          return (
            <div key={step.key} className={`timeline-step ${stepStatus}`}>
              <div className="step-indicator">
                <div className={`step-icon ${stepStatus}`}>{step.icon}</div>
                {!isLast && <div className={`step-line ${stepStatus}`}></div>}
              </div>

              <div className="step-content">
                <div className="step-title">{step.label}</div>
                <div className="step-description">{step.description}</div>

                {/* Mostrar timestamp para pasos completados */}
                {stepStatus === 'completed' && step.key === 'CREATED' && createdAt && (
                  <div className="step-timestamp">{formatDate(createdAt)}</div>
                )}
                {stepStatus === 'completed' && step.key === 'DELIVERED' && deliveredAt && (
                  <div className="step-timestamp">{formatDate(deliveredAt)}</div>
                )}

                {/* Indicador de paso activo */}
                {stepStatus === 'active' && (
                  <div className="step-badge">
                    <FaClock /> En progreso
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>

      {/* Información adicional según el estado */}
      <div className="timeline-info">
        {status === 'CREATED' && (
          <div className="info-message info">
            <FaClock />
            <span>
              Tu encomienda ha sido registrada y está esperando ser asignada a un viaje.
            </span>
          </div>
        )}
        {status === 'IN_TRANSIT' && (
          <div className="info-message warning">
            <FaTruck />
            <span>Tu encomienda está en camino. Pronto llegará a su destino.</span>
          </div>
        )}
        {status === 'DELIVERED' && (
          <div className="info-message success">
            <FaCheckCircle />
            <span>¡Encomienda entregada exitosamente!</span>
          </div>
        )}
        {status === 'FAILED' && (
          <div className="info-message error">
            <FaTimesCircle />
            <span>
              La entrega no pudo completarse. Por favor, contacta con nuestro servicio al
              cliente.
            </span>
          </div>
        )}
      </div>
    </div>
  )
}

export default ParcelTimeline
