import React, { useState, useEffect } from 'react'
import userService from '../../services/userService'
import busService from '../../services/busService'
import routeService from '../../services/routeService'
import tripService from '../../services/tripService'
import Card from '../../components/Card'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaUsers, FaBus, FaRoute, FaCalendarCheck } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalBuses: 0,
    totalRoutes: 0,
    scheduledTrips: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      setLoading(true)

      const [users, buses, routes, trips] = await Promise.all([
        userService.getAllUsers(),
        busService.getAllBuses(),
        routeService.getAllRoutes(),
        tripService.getAllTrips(),
      ])

      const scheduledTrips = trips.filter((trip) => trip.status === 'SCHEDULED').length

      setStats({
        totalUsers: users.length,
        totalBuses: buses.length,
        totalRoutes: routes.length,
        scheduledTrips: scheduledTrips,
      })
    } catch (err) {
      setError('Error al cargar estadísticas: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <Loading message="Cargando dashboard..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Dashboard Administrativo</h1>
          <p>Resumen general del sistema</p>
        </div>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      <div className="dashboard-grid">
        <Card className="stat-card">
          <div className="stat-content">
            <div className="stat-icon users">
              <FaUsers />
            </div>
            <div className="stat-info">
              <h3>Total de Usuarios</h3>
              <p className="stat-number">{stats.totalUsers}</p>
              <small>Usuarios registrados en el sistema</small>
            </div>
          </div>
        </Card>

        <Card className="stat-card">
          <div className="stat-content">
            <div className="stat-icon buses">
              <FaBus />
            </div>
            <div className="stat-info">
              <h3>Total de Buses</h3>
              <p className="stat-number">{stats.totalBuses}</p>
              <small>Buses en la flota</small>
            </div>
          </div>
        </Card>

        <Card className="stat-card">
          <div className="stat-content">
            <div className="stat-icon routes">
              <FaRoute />
            </div>
            <div className="stat-info">
              <h3>Total de Rutas</h3>
              <p className="stat-number">{stats.totalRoutes}</p>
              <small>Rutas disponibles</small>
            </div>
          </div>
        </Card>

        <Card className="stat-card">
          <div className="stat-content">
            <div className="stat-icon trips">
              <FaCalendarCheck />
            </div>
            <div className="stat-info">
              <h3>Viajes Programados</h3>
              <p className="stat-number">{stats.scheduledTrips}</p>
              <small>Viajes pendientes de salida</small>
            </div>
          </div>
        </Card>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <Card>
          <h2>Información del Sistema</h2>
          <p style={{ marginTop: '1rem', color: '#666' }}>
            Este dashboard muestra estadísticas generales del sistema de transporte. Utilice el menú
            lateral para acceder a las diferentes secciones de administración.
          </p>
        </Card>
      </div>
    </div>
  )
}

export default Dashboard
