import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Card from '../components/Card'
import Button from '../components/Button'
import { FaBus, FaBox } from 'react-icons/fa'
import DriverPanelOld from './DriverPanelOld'
import DeliverParcel from './driver/DeliverParcel'
import DriverDashboard from './driver/DriverDashboard'
import DriverParcels from './driver/DriverParcels'
import '../styles/Panel.css'

const DriverPanel = () => {
  return (
    <div className="panel">
      <Routes>
        <Route
          path="/"
          element={
            <div className="panel-home">
              <h1>Panel de Conductor</h1>
              <div className="panel-grid">
                <Card className="panel-card">
                  <FaBus size={48} />
                  <h3>Mis Viajes</h3>
                  <p>Ver y gestionar viajes asignados</p>
                  <Link to="/driver/trips">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaBox size={48} />
                  <h3>Entregar Encomiendas</h3>
                  <p>Gestionar entregas con OTP y foto</p>
                  <Link to="/driver/deliver-parcels">
                    <Button>Acceder</Button>
                  </Link>
                </Card>
              </div>
            </div>
          }
        />
        <Route path="/dashboard" element={<DriverDashboard />} />
        <Route path="/trips" element={<DriverPanelOld />} />
        <Route path="/parcels" element={<DriverParcels />} />
        <Route path="/deliver" element={<DeliverParcel />} />
        <Route path="/deliver-parcels" element={<DeliverParcel />} />
      </Routes>
    </div>
  )
}

export default DriverPanel
