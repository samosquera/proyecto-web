# BERS Transport - Frontend React

Frontend desarrollado en React + Vite para el sistema de gestión de transporte BERS.

## Características

- ✅ Autenticación con JWT
- ✅ Dashboard dinámico según rol de usuario
- ✅ Búsqueda y compra de boletos
- ✅ Gestión de viajes para despachadores
- ✅ Panel de conductor para viajes asignados
- ✅ Panel administrativo completo
- ✅ Interfaz responsive y moderna
- ✅ Integración completa con API backend

## Roles de Usuario

El sistema soporta los siguientes roles:

- **PASSENGER/CUSTOMER**: Búsqueda y compra de boletos
- **DRIVER**: Gestión de viajes asignados
- **CLERK**: Venta rápida y gestión de paquetería
- **DISPATCHER**: Creación y gestión de viajes
- **ADMIN**: Administración completa del sistema

## Requisitos Previos

- Node.js 16+ instalado
- Backend Spring Boot corriendo en `http://localhost:8080`

## Instalación

1. Navegar al directorio frontend:
```bash
cd frontend
```

2. Instalar dependencias:
```bash
npm install
```

3. Configurar variables de entorno:
```bash
cp .env.example .env
```

Editar `.env` si es necesario:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=BERS Transport System
```

## Ejecutar en Desarrollo

```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:3000`

## Build para Producción

```bash
npm run build
```

Los archivos compilados estarán en el directorio `dist/`

## Vista Previa de Build

```bash
npm run preview
```

## Estructura del Proyecto

```
frontend/
├── public/              # Archivos estáticos
├── src/
│   ├── components/      # Componentes reutilizables
│   │   ├── Alert.jsx
│   │   ├── Button.jsx
│   │   ├── Card.jsx
│   │   ├── Input.jsx
│   │   ├── Select.jsx
│   │   ├── Loading.jsx
│   │   ├── Layout.jsx
│   │   ├── Navbar.jsx
│   │   └── ProtectedRoute.jsx
│   ├── context/         # Contextos de React
│   │   └── AuthContext.jsx
│   ├── pages/           # Páginas principales
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Dashboard.jsx
│   │   ├── TripSearch.jsx
│   │   ├── MyTickets.jsx
│   │   ├── AdminPanel.jsx
│   │   ├── DispatcherPanel.jsx
│   │   ├── DriverPanel.jsx
│   │   ├── ClerkPanel.jsx
│   │   └── dispatcher/
│   │       ├── DispatcherTrips.jsx
│   │       └── DispatcherAssignments.jsx
│   ├── services/        # Servicios API
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── tripService.js
│   │   ├── ticketService.js
│   │   ├── routeService.js
│   │   ├── busService.js
│   │   ├── userService.js
│   │   └── seatService.js
│   ├── styles/          # Estilos CSS
│   │   ├── index.css
│   │   ├── App.css
│   │   ├── Auth.css
│   │   ├── Dashboard.css
│   │   ├── TripSearch.css
│   │   ├── MyTickets.css
│   │   ├── DriverPanel.css
│   │   ├── Panel.css
│   │   ├── Navbar.css
│   │   ├── Layout.css
│   │   ├── Button.css
│   │   ├── Card.css
│   │   ├── Input.css
│   │   ├── Select.css
│   │   ├── Loading.css
│   │   └── Alert.css
│   ├── App.jsx          # Componente principal
│   └── main.jsx         # Punto de entrada
├── .env                 # Variables de entorno
├── .env.example         # Ejemplo de variables
├── index.html           # HTML base
├── package.json         # Dependencias
├── vite.config.js       # Configuración de Vite
└── README.md            # Este archivo
```

## Flujo de Uso

### Para Pasajeros

1. **Registro/Login**
   - Crear cuenta en `/register`
   - Iniciar sesión en `/login`

2. **Buscar Viajes**
   - Ir a "Buscar Viajes" desde el dashboard
   - Seleccionar origen, destino y fecha
   - Ver viajes disponibles

3. **Comprar Boleto**
   - Seleccionar viaje
   - Elegir asiento
   - Confirmar reserva

4. **Ver Mis Boletos**
   - Acceder a "Mis Boletos"
   - Ver estado de boletos
   - Cancelar si es necesario

### Para Conductores

1. **Ver Viajes Asignados**
   - Dashboard muestra viajes activos
   - Ver detalles de cada viaje

2. **Gestionar Viaje**
   - Iniciar abordaje (SCHEDULED → BOARDING)
   - Marcar como partido (BOARDING → DEPARTED)
   - Marcar como llegado (DEPARTED → ARRIVED)

### Para Despachadores

1. **Crear Viajes**
   - Ir a "Gestión de Viajes"
   - Seleccionar ruta, bus, fecha y horarios
   - Crear viaje

2. **Gestionar Viajes**
   - Ver viajes del día
   - Cancelar viajes si es necesario
   - Gestionar asignaciones de conductores

### Para Administradores

Acceso completo a:
- Gestión de usuarios
- Gestión de autobuses
- Gestión de rutas
- Configuración del sistema

## Servicios API Implementados

### authService
- `login()` - Inicio de sesión
- `register()` - Registro de usuario
- `logout()` - Cerrar sesión
- `getMe()` - Obtener usuario actual
- `changePassword()` - Cambiar contraseña

### tripService
- `filterTrips()` - Buscar viajes
- `getTripById()` - Obtener viaje por ID
- `createTrip()` - Crear viaje (DISPATCHER/ADMIN)
- `updateTrip()` - Actualizar viaje
- `cancelTrip()` - Cancelar viaje
- `openBoarding()` - Iniciar abordaje
- `departTrip()` - Marcar como partido
- `arriveTrip()` - Marcar como llegado
- `getMyTrips()` - Viajes del conductor

### ticketService
- `createTicket()` - Crear boleto
- `getMyTickets()` - Mis boletos
- `confirmPayment()` - Confirmar pago
- `cancelTicket()` - Cancelar boleto
- `getTicketByQR()` - Obtener por código QR
- `markAsUsed()` - Marcar como usado

### routeService
- `getAllRoutes()` - Todas las rutas
- `getOrigins()` - Orígenes disponibles
- `getDestinationsByOrigin()` - Destinos por origen
- `createRoute()` - Crear ruta (DISPATCHER/ADMIN)
- `updateRoute()` - Actualizar ruta

### busService
- `getAllBuses()` - Todos los autobuses
- `getAvailableBuses()` - Autobuses disponibles
- `createBus()` - Crear autobús (DISPATCHER/ADMIN)
- `updateBus()` - Actualizar autobús
- `changeBusStatus()` - Cambiar estado

### userService
- `getMyProfile()` - Mi perfil
- `updateMyProfile()` - Actualizar perfil
- `getAllUsers()` - Todos los usuarios (ADMIN)
- `createUser()` - Crear usuario (ADMIN)
- `changeUserStatus()` - Cambiar estado de usuario

### seatService
- `getSeatsByBus()` - Asientos de un bus
- `getFullSeatsAndHolds()` - Asientos con ocupación
- `createSeat()` - Crear asiento
- `batchCreateSeats()` - Crear múltiples asientos

## Características de Seguridad

- JWT token en localStorage
- Interceptor de Axios para agregar token automáticamente
- Redirección automática al login si el token expira
- Rutas protegidas por rol
- Validación de permisos en componentes

## Proxy de Desarrollo

El proxy está configurado en `vite.config.js` para redirigir peticiones `/api/*` al backend:

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

## Troubleshooting

### Error de CORS
- Asegúrate de que el backend tenga configurado CORS
- Verifica que el backend esté corriendo en el puerto correcto

### Error 401 Unauthorized
- El token JWT ha expirado
- Cierra sesión y vuelve a iniciar sesión

### No se cargan los datos
- Verifica que el backend esté corriendo
- Revisa la consola del navegador para ver errores
- Verifica la configuración de `.env`

## Tecnologías Utilizadas

- **React 18** - Librería de UI
- **Vite** - Build tool y dev server
- **React Router 6** - Enrutamiento
- **Axios** - Cliente HTTP
- **React Icons** - Iconos
- **date-fns** - Manejo de fechas
- **CSS Modules** - Estilos

## Próximas Mejoras

- [ ] Integración de pagos reales
- [ ] Notificaciones en tiempo real
- [ ] Generación de reportes
- [ ] Chat de soporte
- [ ] App móvil con React Native
- [ ] PWA (Progressive Web App)
- [ ] Tests unitarios y de integración
- [ ] Internacionalización (i18n)

## Soporte

Para reportar bugs o solicitar features, crear un issue en el repositorio.

## Licencia

Propiedad de BERS Transport System
