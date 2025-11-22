import React, { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import LoginModal from './LoginModal'
import RegisterModal from './RegisterModal'
import '../styles/Layout.css'

const Layout = () => {
  const [loginModalOpen, setLoginModalOpen] = useState(false)
  const [registerModalOpen, setRegisterModalOpen] = useState(false)

  const handleOpenLogin = () => {
    setRegisterModalOpen(false)
    setLoginModalOpen(true)
  }

  const handleOpenRegister = () => {
    setLoginModalOpen(false)
    setRegisterModalOpen(true)
  }

  return (
    <div className="layout">
      <Sidebar onOpenLogin={handleOpenLogin} onOpenRegister={handleOpenRegister} />
      <main className="main-content">
        <Outlet />
      </main>

      <LoginModal
        isOpen={loginModalOpen}
        onClose={() => setLoginModalOpen(false)}
        onSwitchToRegister={handleOpenRegister}
      />

      <RegisterModal
        isOpen={registerModalOpen}
        onClose={() => setRegisterModalOpen(false)}
        onSwitchToLogin={handleOpenLogin}
      />
    </div>
  )
}

export default Layout
