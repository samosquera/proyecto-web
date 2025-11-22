import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Card from '../components/Card'
import Button from '../components/Button'
import {
  FaBus,
  FaClipboardList,
  FaUsers,
  FaExclamationTriangle,
  FaPlayCircle,
} from 'react-icons/fa'
import DispatcherTrips from './dispatcher/DispatcherTrips'
import DispatcherAssignments from './dispatcher/DispatcherAssignments'
import DispatcherDispatch from './dispatcher/DispatcherDispatch'
import ManageOverbooking from './dispatcher/ManageOverbooking'
import ManageIncidents from './dispatcher/ManageIncidents'
import '../styles/Panel.css'

const DispatcherPanel = () => {
  return (
    <div className="panel">
      <Routes>
        <Route
          path="/"
          element={
            <div className="panel-home">
              <h1>Panel de Despachador</h1>
              <div className="panel-grid">
                <Card className="panel-card">
                  <FaBus size={48} />
                  <h3>Gestión de Viajes</h3>
                  <p>Crear, modificar y monitorear viajes</p>
                  <Link to="/dispatcher/trips">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaClipboardList size={48} />
                  <h3>Asignaciones</h3>
                  <p>Asignar conductores a viajes</p>
                  <Link to="/dispatcher/assignments">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaPlayCircle size={48} />
                  <h3>Panel de Despacho</h3>
                  <p>Gestionar abordaje y salida de viajes</p>
                  <Link to="/dispatcher/dispatch">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaExclamationTriangle size={48} />
                  <h3>Incidentes</h3>
                  <p>Reportar y gestionar incidentes</p>
                  <Link to="/dispatcher/incidents">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaUsers size={48} />
                  <h3>Overbooking</h3>
                  <p>Gestionar solicitudes de overbooking</p>
                  <Link to="/dispatcher/overbooking">
                    <Button>Acceder</Button>
                  </Link>
                </Card>
              </div>
            </div>
          }
        />
        <Route path="/trips" element={<DispatcherTrips />} />
        <Route path="/assignments" element={<DispatcherAssignments />} />
        <Route path="/dispatch" element={<DispatcherDispatch />} />
        <Route path="/incidents" element={<ManageIncidents />} />
        <Route path="/overbooking" element={<ManageOverbooking />} />
      </Routes>
    </div>
  )
}

export default DispatcherPanel
