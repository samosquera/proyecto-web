import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import Card from '../components/Card'
import Button from '../components/Button'
import { FaTicketAlt, FaTruck, FaSuitcase } from 'react-icons/fa'
import CreateParcel from './clerk/CreateParcel'
import ClerkDashboard from './clerk/ClerkDashboard'
import QuickSale from './clerk/QuickSale'
import ManageBaggage from './clerk/ManageBaggage'
import '../styles/Panel.css'

const ClerkPanel = () => {
  return (
    <div className="panel">
      <Routes>
        <Route
          path="/"
          element={
            <div className="panel-home">
              <h1>Panel de Empleado</h1>
              <div className="panel-grid">
                <Card className="panel-card">
                  <FaTicketAlt size={48} />
                  <h3>Venta Rápida</h3>
                  <p>Vender boletos rápidamente</p>
                  <Link to="/clerk/sales">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaTruck size={48} />
                  <h3>Paquetería</h3>
                  <p>Gestión de paquetes</p>
                  <Link to="/clerk/parcels">
                    <Button>Acceder</Button>
                  </Link>
                </Card>

                <Card className="panel-card">
                  <FaSuitcase size={48} />
                  <h3>Equipaje</h3>
                  <p>Registrar y gestionar equipaje</p>
                  <Link to="/clerk/baggage">
                    <Button>Acceder</Button>
                  </Link>
                </Card>
              </div>
            </div>
          }
        />
        <Route path="/dashboard" element={<ClerkDashboard />} />
        <Route path="/sales" element={<QuickSale />} />
        <Route path="/parcels" element={<CreateParcel />} />
        <Route path="/baggage" element={<ManageBaggage />} />
      </Routes>
    </div>
  )
}

export default ClerkPanel
