import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import incidentService from '../../services/incidentService'
import { FaExclamationTriangle, FaPlus } from 'react-icons/fa'

const ManageIncidents = () => {
  const [incidents, setIncidents] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({ type: 'ACCIDENT', entityType: 'TRIP', entityId: '', description: '' })

  useEffect(() => { loadIncidents() }, [])

  const loadIncidents = async () => {
    try {
      setLoading(true)
      const data = await incidentService.getAllIncidents()
      setIncidents(data)
    } catch (err) {
      setError('Error al cargar incidentes')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      setLoading(true)
      await incidentService.createIncident({ ...formData, entityId: parseInt(formData.entityId) })
      setSuccess('Incidente registrado exitosamente')
      setShowForm(false)
      setFormData({ type: 'ACCIDENT', entityType: 'TRIP', entityId: '', description: '' })
      loadIncidents()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrar incidente')
    } finally {
      setLoading(false)
    }
  }

  if (loading && incidents.length === 0) return <Loading message="Cargando incidentes..." />

  return (
    <div style={{maxWidth: '1200px', margin: '0 auto', padding: '24px'}}>
      <div style={{marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div><h1>Gestión de Incidentes</h1><p style={{color: 'var(--text-secondary)'}}>Reportar y gestionar incidentes del sistema</p></div>
        <Button onClick={() => setShowForm(!showForm)} icon={<FaPlus />}>{showForm ? 'Cancelar' : 'Reportar Incidente'}</Button>
      </div>
      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}
      {showForm && (
        <Card style={{marginBottom: '24px'}}>
          <h3>Nuevo Incidente</h3>
          <form onSubmit={handleSubmit}>
            <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px', marginTop: '16px'}}>
              <Select label="Tipo" name="type" value={formData.type} onChange={handleInputChange} required options={[{value: 'ACCIDENT', label: 'Accidente'}, {value: 'MECHANICAL_FAILURE', label: 'Falla Mecánica'}, {value: 'DELAY', label: 'Retraso'}, {value: 'CANCELLATION', label: 'Cancelación'}, {value: 'OTHER', label: 'Otro'}]} />
              <Select label="Entidad" name="entityType" value={formData.entityType} onChange={handleInputChange} required options={[{value: 'TRIP', label: 'Viaje'}, {value: 'BUS', label: 'Bus'}, {value: 'DRIVER', label: 'Conductor'}, {value: 'ROUTE', label: 'Ruta'}]} />
              <Input label="ID de Entidad" name="entityId" type="number" value={formData.entityId} onChange={handleInputChange} required placeholder="ID del viaje/bus/etc" />
            </div>
            <div style={{marginTop: '16px'}}>
              <label style={{display: 'block', marginBottom: '8px', fontWeight: 500}}>Descripción</label>
              <textarea name="description" value={formData.description} onChange={handleInputChange} required placeholder="Describe el incidente..." style={{width: '100%', minHeight: '100px', padding: '12px', border: '1px solid var(--border-color)', borderRadius: '8px', fontSize: '14px', fontFamily: 'inherit'}} />
            </div>
            <div style={{marginTop: '24px', display: 'flex', gap: '12px', justifyContent: 'flex-end'}}>
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>Cancelar</Button>
              <Button type="submit" loading={loading}>Reportar Incidente</Button>
            </div>
          </form>
        </Card>
      )}
      <Card>
        <h3>Incidentes Registrados ({incidents.length})</h3>
        {incidents.length === 0 ? <p style={{textAlign: 'center', padding: '40px', color: 'var(--text-secondary)'}}>No hay incidentes registrados</p> : (
          <div style={{display: 'grid', gap: '12px', marginTop: '16px'}}>
            {incidents.map(incident => (
              <Card key={incident.id} style={{backgroundColor: '#f8f9fa'}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'start'}}>
                  <div style={{flex: 1}}>
                    <div style={{marginBottom: '8px'}}>
                      <span style={{backgroundColor: '#e74c3c', color: 'white', padding: '4px 8px', borderRadius: '4px', fontSize: '0.85rem', marginRight: '8px'}}>{incident.type}</span>
                      <span style={{color: '#666', fontSize: '0.9rem'}}>{incident.entityType} #{incident.entityId}</span>
                    </div>
                    <div style={{color: '#666', fontSize: '0.9rem', marginTop: '8px'}}>{incident.description}</div>
                    <div style={{color: '#999', fontSize: '0.85rem', marginTop: '8px'}}>{new Date(incident.reportedAt).toLocaleString()}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </Card>
    </div>
  )
}

export default ManageIncidents
