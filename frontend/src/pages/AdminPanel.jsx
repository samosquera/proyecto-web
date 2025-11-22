import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Card from '../components/Card'
import Button from '../components/Button'
import { FaUsers, FaBus, FaRoute, FaCog } from 'react-icons/fa'
import Dashboard from './admin/Dashboard'
import ManageUsers from './admin/ManageUsers'
import ManageBuses from './admin/ManageBuses'
import ManageRoutes from './admin/ManageRoutes'
import ManageTrips from './admin/ManageTrips'
import ManageParcels from './admin/ManageParcels'
import SystemSettings from './admin/SystemSettings'
import '../styles/Panel.css'

const AdminPanel = () => {
  return (
    <div className="panel">
      <Routes>
        <Route
          path="/"
          element={
            <div className="panel-home">
              <h1>Panel de Administración</h1>
              <div className="panel-grid">
                <Card className="panel-card">
                  <FaUsers size={48} />
                  <h3>Usuarios</h3>
                  <p>Gestionar usuarios del sistema</p>
                  <Link to="/admin/users">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaBus size={48} />
                  <h3>Autobuses</h3>
                  <p>Gestionar flota de autobuses</p>
                  <Link to="/admin/buses">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaRoute size={48} />
                  <h3>Rutas</h3>
                  <p>Gestionar rutas y paradas</p>
                  <Link to="/admin/routes">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaCog size={48} />
                  <h3>Configuración</h3>
                  <p>Configuración del sistema</p>
                  <Link to="/admin/settings">
                    <Button>Acceder</Button>
                  </Link>
                </Card>
              </div>
            </div>
          }
        />

        {/* Rutas funcionales con componentes implementados */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/users" element={<ManageUsers />} />
        <Route path="/buses" element={<ManageBuses />} />
        <Route path="/routes" element={<ManageRoutes />} />
        <Route path="/trips" element={<ManageTrips />} />
        <Route path="/parcels" element={<ManageParcels />} />
        <Route path="/settings" element={<SystemSettings />} />
      </Routes>
    </div>
  )
}

export default AdminPanel
