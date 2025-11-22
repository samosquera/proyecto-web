import React, { useState, useEffect } from 'react'
import userService from '../../services/userService'
import Card from '../../components/Card'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import Loading from '../../components/Loading'
import Alert from '../../components/Alert'
import { FaPlus, FaEdit, FaTrash, FaSearch, FaUserCircle } from 'react-icons/fa'
import '../../styles/AdminTable.css'

const ManageUsers = () => {
  const [users, setUsers] = useState([])
  const [filteredUsers, setFilteredUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showModal, setShowModal] = useState(false)
  const [editingUser, setEditingUser] = useState(null)

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    phone: '',
    password: '',
    dateOfBirth: '',
    role: 'PASSENGER',
  })

  useEffect(() => {
    loadUsers()
  }, [])

  useEffect(() => {
    filterUsers()
  }, [users, searchTerm, roleFilter, statusFilter])

  const loadUsers = async () => {
    try {
      setLoading(true)
      const data = await userService.getAllUsers()
      setUsers(data)
    } catch (err) {
      setError('Error al cargar usuarios: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const filterUsers = () => {
    let filtered = users

    if (roleFilter !== 'ALL') {
      filtered = filtered.filter((u) => u.role === roleFilter)
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((u) => u.status === statusFilter)
    }

    if (searchTerm) {
      filtered = filtered.filter(
        (u) =>
          u.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          u.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          u.phone?.includes(searchTerm)
      )
    }

    setFilteredUsers(filtered)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    try {
      setLoading(true)
      if (editingUser) {
        // Para actualizar, solo enviamos username, phone y status
        const updateData = {
          username: formData.username,
          phone: formData.phone,
          status: formData.status || 'ACTIVE'
        }
        await userService.updateUser(editingUser.id, updateData)
        setSuccess('Usuario actualizado exitosamente')
      } else {
        await userService.createUser(formData)
        setSuccess('Usuario creado exitosamente')
      }
      setShowModal(false)
      setFormData({
        username: '',
        email: '',
        phone: '',
        password: '',
        dateOfBirth: '',
        role: 'PASSENGER',
      })
      setEditingUser(null)
      loadUsers()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar usuario')
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (user) => {
    setEditingUser(user)
    setFormData({
      username: user.username || '',
      email: user.email || '',
      phone: user.phone || '',
      password: '',
      dateOfBirth: user.dateOfBirth || '',
      role: user.role || 'PASSENGER',
      status: user.status || 'ACTIVE',
    })
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar este usuario?')) return

    try {
      setLoading(true)
      await userService.deleteUser(id)
      setSuccess('Usuario eliminado exitosamente')
      loadUsers()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al eliminar usuario')
    } finally {
      setLoading(false)
    }
  }

  const handleChangeStatus = async (id, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

    try {
      setLoading(true)
      await userService.changeUserStatus(id, newStatus)
      setSuccess('Estado actualizado exitosamente')
      loadUsers()
    } catch (err) {
      setError(err.response?.data?.message || 'Error al cambiar estado')
    } finally {
      setLoading(false)
    }
  }

  const getRoleLabel = (role) => {
    const labels = {
      ADMIN: 'Administrador',
      DISPATCHER: 'Despachador',
      DRIVER: 'Conductor',
      CLERK: 'Empleado',
      PASSENGER: 'Pasajero',
      CUSTOMER: 'Cliente',
    }
    return labels[role] || role
  }

  if (loading && users.length === 0) {
    return <Loading message="Cargando usuarios..." />
  }

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1>Gestión de Usuarios</h1>
          <p>Administre los usuarios del sistema</p>
        </div>
        <Button onClick={() => setShowModal(true)} icon={<FaPlus />}>
          Nuevo Usuario
        </Button>
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError('')} />}
      {success && <Alert type="success" message={success} onClose={() => setSuccess('')} />}

      <Card className="filters-card">
        <div className="filters-grid">
          <Input
            placeholder="Buscar por nombre, email o teléfono..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<FaSearch />}
          />
          <Select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            options={[
              { value: 'ALL', label: 'Todos los roles' },
              { value: 'ADMIN', label: 'Administradores' },
              { value: 'DISPATCHER', label: 'Despachadores' },
              { value: 'DRIVER', label: 'Conductores' },
              { value: 'CLERK', label: 'Empleados' },
              { value: 'PASSENGER', label: 'Pasajeros' },
            ]}
          />
          <Select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            options={[
              { value: 'ALL', label: 'Todos los estados' },
              { value: 'ACTIVE', label: 'Activos' },
              { value: 'INACTIVE', label: 'Inactivos' },
            ]}
          />
        </div>
      </Card>

      <Card className="table-card">
        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Email</th>
                <th>Teléfono</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center">
                    No se encontraron usuarios
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>
                      <div className="user-cell">
                        <FaUserCircle />
                        <span>{user.username}</span>
                      </div>
                    </td>
                    <td>{user.email}</td>
                    <td>{user.phone}</td>
                    <td>
                      <span className={`badge badge-${user.role?.toLowerCase()}`}>
                        {getRoleLabel(user.role)}
                      </span>
                    </td>
                    <td>
                      <button
                        className={`status-badge ${user.status?.toLowerCase()}`}
                        onClick={() => handleChangeStatus(user.id, user.status)}
                      >
                        {user.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}
                      </button>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn-icon" onClick={() => handleEdit(user)} title="Editar">
                          <FaEdit />
                        </button>
                        <button
                          className="btn-icon btn-danger"
                          onClick={() => handleDelete(user.id)}
                          title="Eliminar"
                        >
                          <FaTrash />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-grid">
                <Input
                  label="Nombre de Usuario"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  required
                />
                <Input
                  label="Email"
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  required={!editingUser}
                  disabled={editingUser}
                />
                <Input
                  label="Teléfono (10 dígitos)"
                  name="phone"
                  value={formData.phone}
                  onChange={handleInputChange}
                  pattern="\d{10}"
                  maxLength="10"
                  required
                />
                <Input
                  label="Contraseña"
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  required={!editingUser}
                  placeholder={editingUser ? 'No se puede cambiar al editar' : 'Mínimo 8 caracteres'}
                  disabled={editingUser}
                  minLength="8"
                />
                <Input
                  label="Fecha de Nacimiento"
                  type="date"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleInputChange}
                  required={!editingUser}
                  disabled={editingUser}
                />
                <Select
                  label="Rol"
                  name="role"
                  value={formData.role}
                  onChange={handleInputChange}
                  options={[
                    { value: 'ADMIN', label: 'Administrador' },
                    { value: 'DISPATCHER', label: 'Despachador' },
                    { value: 'DRIVER', label: 'Conductor' },
                    { value: 'CLERK', label: 'Empleado' },
                    { value: 'PASSENGER', label: 'Pasajero' },
                  ]}
                  required={!editingUser}
                  disabled={editingUser}
                />
                {editingUser && (
                  <Select
                    label="Estado"
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    options={[
                      { value: 'ACTIVE', label: 'Activo' },
                      { value: 'INACTIVE', label: 'Inactivo' },
                    ]}
                    required
                  />
                )}
              </div>
              {editingUser && (
                <Alert
                  type="info"
                  message="Al editar solo se pueden cambiar: Nombre de usuario, Teléfono y Estado"
                />
              )}
              <div className="modal-actions">
                <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                  Cancelar
                </Button>
                <Button type="submit" loading={loading}>
                  {editingUser ? 'Actualizar' : 'Crear'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default ManageUsers
