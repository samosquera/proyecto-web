import React from 'react'
import { FaBus, FaShieldAlt, FaClock, FaDollarSign, FaUsers, FaRoute, FaPhone, FaEnvelope, FaMapMarkerAlt } from 'react-icons/fa'
import Card from '../components/Card'
import '../styles/About.css'

const About = () => {
  const features = [
    {
      icon: <FaBus />,
      title: 'Flota Moderna',
      description:
        'Contamos con buses modernos y cómodos equipados con las últimas tecnologías para tu seguridad y comodidad.',
    },
    {
      icon: <FaShieldAlt />,
      title: 'Viajes Seguros',
      description:
        'Todos nuestros conductores están certificados y nuestros vehículos pasan rigurosas inspecciones de seguridad.',
    },
    {
      icon: <FaClock />,
      title: 'Puntualidad',
      description:
        'Nos comprometemos a cumplir con los horarios establecidos para que llegues a tiempo a tu destino.',
    },
    {
      icon: <FaDollarSign />,
      title: 'Precios Justos',
      description:
        'Ofrecemos tarifas competitivas y transparentes en pesos colombianos (COP), sin cargos ocultos ni sorpresas.',
    },
    {
      icon: <FaRoute />,
      title: 'Múltiples Rutas',
      description:
        'Conectamos las principales ciudades de Colombia con rutas directas y cómodas para tu conveniencia.',
    },
    {
      icon: <FaUsers />,
      title: 'Atención al Cliente 24/7',
      description:
        'Nuestro equipo está disponible las 24 horas del día para ayudarte antes, durante y después de tu viaje.',
    },
  ]

  return (
    <div className="about-page">
      <div className="about-header">
        <FaBus className="about-logo" />
        <h1>Sobre BERS App</h1>
        <p className="about-subtitle">
          Tu compañero de confianza para viajes terrestres en Colombia
        </p>
      </div>

      <Card className="about-intro">
        <h2>¿Quiénes Somos?</h2>
        <p>
          BERS App es una plataforma líder en el sector de transporte terrestre de pasajeros y
          encomiendas en Colombia. Con tecnología de punta y enfoque en la experiencia del usuario,
          nos hemos consolidado como una de las opciones más confiables y preferidas por miles de colombianos.
        </p>
        <p>
          Nuestra misión es conectar personas y comunidades a través de un servicio de transporte
          seguro, cómodo y eficiente, contribuyendo al desarrollo económico y social del país.
        </p>
      </Card>

      <div className="features-section">
        <h2>¿Por Qué Elegirnos?</h2>
        <div className="features-grid">
          {features.map((feature, index) => (
            <Card key={index} className="feature-card">
              <div className="feature-icon">{feature.icon}</div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </Card>
          ))}
        </div>
      </div>

      <Card className="vision-section">
        <h2>Nuestra Visión</h2>
        <p>
          Ser la plataforma de transporte terrestre más moderna y tecnológica de Colombia, reconocida
          por la excelencia en el servicio, la innovación constante y el compromiso con la
          seguridad y satisfacción de nuestros pasajeros.
        </p>
      </Card>

      <Card className="values-section">
        <h2>Nuestros Valores</h2>
        <ul className="values-list">
          <li>
            <strong>Seguridad:</strong> La seguridad de nuestros pasajeros es nuestra prioridad
            número uno.
          </li>
          <li>
            <strong>Compromiso:</strong> Cumplimos nuestras promesas y superamos las expectativas.
          </li>
          <li>
            <strong>Integridad:</strong> Actuamos con honestidad y transparencia en todas nuestras
            operaciones.
          </li>
          <li>
            <strong>Innovación:</strong> Buscamos constantemente mejorar nuestros servicios con
            nuevas tecnologías.
          </li>
          <li>
            <strong>Respeto:</strong> Tratamos a todos nuestros clientes y empleados con dignidad y
            respeto.
          </li>
        </ul>
      </Card>

      <div className="contact-section">
        <h2>Contáctanos</h2>
        <Card className="contact-info">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <FaPhone style={{ color: 'var(--primary-color)' }} />
            <div>
              <strong>Teléfono:</strong> +57 3044107452
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <FaEnvelope style={{ color: 'var(--primary-color)' }} />
            <div>
              <strong>Email:</strong> support@bersapp.co
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <FaMapMarkerAlt style={{ color: 'var(--primary-color)' }} />
            <div>
              <strong>Dirección:</strong> Calle 100 #15-20, Bogotá D.C., Colombia
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FaClock style={{ color: 'var(--primary-color)' }} />
            <div>
              <strong>Horario de atención:</strong> 24 horas, 7 días a la semana
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}

export default About
