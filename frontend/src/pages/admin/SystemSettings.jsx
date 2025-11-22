import React, { useState, useEffect } from 'react'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import configService from '../../services/configService'
import { FaCog, FaPlus, FaEdit, FaTrash } from 'react-icons/fa'

const SystemSettings = () => {
  const [configs, setConfigs] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingConfig, setEditingConfig] = useState(null)
  const [formData, setFormData] = useState({ key: '', value: '', description: '' })

  useEffect(() => { loadConfigs() }, [])

  const loadConfigs = async () => {
    try {
      setLoading(true)
      const data = await configService.getAllConfigs()
      setConfigs(data)
    } catch (err) {
      setError('Error al cargar configuraciones')
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
      if (editingConfig) {
        await configService.updateConfig(editingConfig.id, formData)
        setSuccess('Configuración actualizada exitosamente')
      } else {
        await configService.createConfig(formData)
        setSuccess('Configuración creada exitosamente')
      }
      setShowForm(false)
      setEditingConfig(null)
      setFormData({ key: '', value: '', description: '' })
      loadConfigs()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar configuración')
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (config) => {
    setEditingConfig(config)
    setFormData({ key: config.key, value: config.value, description: config.description || '' })
    setShowForm(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar esta configuración?')) return
    try {
      setLoading(true)
      await configService.deleteConfig(id)
      setSuccess('Configuración eliminada exitosamente')
      loadConfigs()
    } catch (err) {
      setError('Error al eliminar configuración')
    } finally {
      setLoading(false)
    }
  }

  if (loading && configs.length === 0) return <Loading message="Cargando configuraciones..." />

  return (
    <div style={{maxWidth: '1200px', margin: '0 auto', padding: '24px'}}>
      <div style={{marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
        <div><h1>Configuración del Sistema</h1><p style={{color: 'var(--text-secondary)'}}>Gestionar parámetros de configuración</p></div>
        <Button onClick={() => setShowForm(!showForm)} icon={<FaPlus />}>{showForm ? 'Cancelar' : 'Nueva Configuración'}</Button>
      </div>
      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}
      {showForm && (
        <Card style={{marginBottom: '24px'}}>
          <h3>{editingConfig ? 'Editar Configuración' : 'Nueva Configuración'}</h3>
          <form onSubmit={handleSubmit}>
            <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '16px', marginTop: '16px'}}>
              <Input label="Clave" name="key" value={formData.key} onChange={handleInputChange} required disabled={!!editingConfig} placeholder="ej: MAX_BAGGAGE_WEIGHT" />
              <Input label="Valor" name="value" value={formData.value} onChange={handleInputChange} required placeholder="ej: 25" />
              <Input label="Descripción" name="description" value={formData.description} onChange={handleInputChange} placeholder="Peso máximo de equipaje en kg" />
            </div>
            <div style={{marginTop: '24px', display: 'flex', gap: '12px', justifyContent: 'flex-end'}}>
              <Button type="button" variant="secondary" onClick={() => { setShowForm(false); setEditingConfig(null); setFormData({ key: '', value: '', description: '' }) }}>Cancelar</Button>
              <Button type="submit" loading={loading}>{editingConfig ? 'Actualizar' : 'Crear'}</Button>
            </div>
          </form>
        </Card>
      )}
      <Card>
        <h3>Configuraciones del Sistema ({configs.length})</h3>
        {configs.length === 0 ? <p style={{textAlign: 'center', padding: '40px', color: 'var(--text-secondary)'}}>No hay configuraciones registradas</p> : (
          <div style={{overflowX: 'auto'}}>
            <table style={{width: '100%', borderCollapse: 'collapse'}}>
              <thead><tr style={{borderBottom: '2px solid var(--border-color)'}}><th style={{padding: '12px', textAlign: 'left'}}>Clave</th><th style={{padding: '12px', textAlign: 'left'}}>Valor</th><th style={{padding: '12px', textAlign: 'left'}}>Descripción</th><th style={{padding: '12px', textAlign: 'center'}}>Acciones</th></tr></thead>
              <tbody>
                {configs.map(config => (
                  <tr key={config.id} style={{borderBottom: '1px solid var(--border-color)'}}>
                    <td style={{padding: '12px', fontFamily: 'monospace'}}>{config.key}</td>
                    <td style={{padding: '12px', fontWeight: 'bold'}}>{config.value}</td>
                    <td style={{padding: '12px', color: '#666'}}>{config.description || '-'}</td>
                    <td style={{padding: '12px', textAlign: 'center'}}>
                      <button onClick={() => handleEdit(config)} style={{padding: '4px 8px', marginRight: '8px', background: 'var(--accent-color)', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}><FaEdit /></button>
                      <button onClick={() => handleDelete(config.id)} style={{padding: '4px 8px', background: '#e74c3c', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer'}}><FaTrash /></button>
                    </td>
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

export default SystemSettings
