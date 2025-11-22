import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import TripSearch from './pages/TripSearch'
import CreateParcel from './pages/CreateParcel'
import About from './pages/About'
import PaymentView from './pages/PaymentView'
import MyTickets from './pages/MyTickets'
import TrackParcel from './pages/TrackParcel'
import AdminPanel from './pages/AdminPanel'
import DispatcherPanel from './pages/DispatcherPanel'
import DriverPanel from './pages/DriverPanel'
import ClerkPanel from './pages/ClerkPanel'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import './styles/App.css'

function App() {
  return (
    <Routes>
      {/* Todas las rutas usan el Layout con Sidebar */}
      <Route element={<Layout />}>
        {/* Ruta principal - Búsqueda de viajes (pública) */}
        <Route path="/" element={<TripSearch />} />

        {/* Rutas públicas */}
        <Route path="/parcels" element={<CreateParcel />} />
        <Route path="/about" element={<About />} />

        {/* Rutas protegidas para pasajeros */}
        <Route
          path="/payment"
          element={
            <ProtectedRoute>
              <PaymentView />
            </ProtectedRoute>
          }
        />

        <Route
          path="/my-tickets"
          element={
            <ProtectedRoute>
              <MyTickets />
            </ProtectedRoute>
          }
        />

        <Route
          path="/track-parcel"
          element={
            <ProtectedRoute>
              <TrackParcel />
            </ProtectedRoute>
          }
        />

        {/* Panel de administrador */}
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AdminPanel />
            </ProtectedRoute>
          }
        />

        {/* Panel de dispatcher */}
        <Route
          path="/dispatcher/*"
          element={
            <ProtectedRoute roles={['DISPATCHER', 'ADMIN']}>
              <DispatcherPanel />
            </ProtectedRoute>
          }
        />

        {/* Panel de conductor */}
        <Route
          path="/driver/*"
          element={
            <ProtectedRoute roles={['DRIVER']}>
              <DriverPanel />
            </ProtectedRoute>
          }
        />

        {/* Panel de clerk */}
        <Route
          path="/clerk/*"
          element={
            <ProtectedRoute roles={['CLERK', 'ADMIN']}>
              <ClerkPanel />
            </ProtectedRoute>
          }
        />

        {/* Ruta por defecto */}
        <Route path="*" element={<Navigate to="/" />} />
      </Route>
    </Routes>
  )
}

export default App
