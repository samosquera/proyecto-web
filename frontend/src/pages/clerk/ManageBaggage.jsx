import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import baggageService from '../../services/baggageService'
import { FaSuitcase, FaWeight, FaDollarSign, FaBarcode } from 'react-icons/fa'

const ManageBaggage = () => {
  const [baggages, setBaggages] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({ ticketId: '', weightKg: '', description: '' })
  const [calculatedFee, setCalculatedFee] = useState(null)

  useEffect(() => { loadBaggages() }, [])

  const loadBaggages = async () => {
    try {
      setLoading(true)
      const data = await baggageService.getAllBaggage()
      setBaggages(data)
    } catch (err) {
      setError('Error al cargar equipajes')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = async (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (name === 'weightKg' && value) {
      try {
        const fee = await baggageService.calculateBaggageFee(parseFloat(value))
        setCalculatedFee(fee)
      } catch (err) { setCalculatedFee(null) }
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      setLoading(true)
      await baggageService.createBaggage({ 
        ticketId: parseInt(formData.ticketId), 
        weightKg: parseFloat(formData.weightKg), 
        description: formData.description || null 
      })
      setSuccess('Equipaje registrado exitosamente')
      setShowForm(false)
      setFormData({ ticketId: '', weightKg: '', description: '' })
      setCalculatedFee(null)
      loadBaggages()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al registrar equipaje')
    } finally {
      setLoading(false)
    }
  }

  if (loading && baggages.length === 0) return <Loading message="Cargando equipajes..." />

  return (
    <div style={{maxWidth: '1200px', margin: '0 auto', padding: '24px'}}>
      <div style={{marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div>
          <h1>Gestión de Equipaje</h1>
          <p style={{color: 'var(--text-secondary)'}}>Registrar y gestionar equipaje de pasajeros</p>
        </div>
        <Button onClick={() => setShowForm(!showForm)} icon={<FaSuitcase />}>
          {showForm ? 'Cancelar' : 'Registrar Equipaje'}
        </Button>
      </div>
      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}
      {showForm && (
        <Card style={{marginBottom: '24px'}}>
          <h3>Nuevo Equipaje</h3>
          <form onSubmit={handleSubmit}>
            <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px', marginTop: '16px'}}>
              <Input label="ID del Boleto" name="ticketId" type="number" value={formData.ticketId} onChange={handleInputChange} required icon={<FaBarcode />} placeholder="Ingrese ID del boleto" />
              <Input label="Peso (kg)" name="weightKg" type="number" step="0.1" value={formData.weightKg} onChange={handleInputChange} required icon={<FaWeight />} placeholder="0.0" />
              <Input label="Descripción (opcional)" name="description" value={formData.description} onChange={handleInputChange} placeholder="Ej: Maleta grande azul" />
            </div>
            {calculatedFee !== null && (
              <div style={{marginTop: '16px', padding: '12px', background: 'rgba(2, 195, 154, 0.1)', borderRadius: '8px'}}>
                <strong>Tarifa calculada: ${calculatedFee.toFixed(2)}</strong>
              </div>
            )}
            <div style={{marginTop: '24px', display: 'flex', gap: '12px', justifyContent: 'flex-end'}}>
              <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>Cancelar</Button>
              <Button type="submit" loading={loading}>Registrar Equipaje</Button>
            </div>
          </form>
        </Card>
      )}
      <Card>
        <h3>Equipajes Registrados ({baggages.length})</h3>
        {baggages.length === 0 ? (
          <p style={{textAlign: 'center', padding: '40px', color: 'var(--text-secondary)'}}>No hay equipajes registrados</p>
        ) : (
          <div style={{overflowX: 'auto'}}>
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
              <thead>
                <tr style={{borderBottom: '2px solid var(--border-color)'}}>
                  <th style={{padding: '12px', textAlign: 'left'}}>Etiqueta</th>
                  <th style={{padding: '12px', textAlign: 'left'}}>Boleto ID</th>
                  <th style={{padding: '12px', textAlign: 'left'}}>Peso (kg)</th>
                  <th style={{padding: '12px', textAlign: 'left'}}>Tarifa</th>
                  <th style={{padding: '12px', textAlign: 'left'}}>Descripción</th>
                </tr>
              </thead>
              <tbody>
                {baggages.map(baggage => (
                  <tr key={baggage.id} style={{borderBottom: '1px solid var(--border-color)'}}>
                    <td style={{padding: '12px'}}>{baggage.tagCode}</td>
                    <td style={{padding: '12px'}}>{baggage.ticketId}</td>
                    <td style={{padding: '12px'}}>{baggage.weightKg} kg</td>
                    <td style={{padding: '12px'}}>${baggage.fee?.toFixed(2) || '0.00'}</td>
                    <td style={{padding: '12px'}}>{baggage.description || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}

export default ManageBaggage
