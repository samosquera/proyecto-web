import React from 'react'
import '../styles/Button.css'

const Button = ({
  children,
  onClick,
  type = 'button',
  variant = 'primary',
  size = 'medium',
  disabled = false,
  fullWidth = false,
  loading = false,
  icon,
  className = '',
}) => {
  const btnClass = `btn btn-${variant} btn-${size} ${fullWidth ? 'btn-full-width' : ''} ${
    loading ? 'btn-loading' : ''
  } ${className}`.trim()

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={btnClass}
    >
      {loading ? (
        <span className="btn-loader"></span>
      ) : (
        <>
          {icon && <span className="btn-icon">{icon}</span>}
          {children}
        </>
      )}
    </button>
  )
}

export default Button
