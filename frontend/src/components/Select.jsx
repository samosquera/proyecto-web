import React from 'react'
import '../styles/Select.css'

const Select = ({
  label,
  name,
  value,
  onChange,
  options = [],
  placeholder = 'Seleccione...',
  required = false,
  disabled = false,
  error,
  icon,
  className = '',
}) => {
  return (
    <div className={`select-group ${className}`}>
      {label && (
        <label htmlFor={name} className="select-label">
          {label}
          {required && <span className="required">*</span>}
        </label>
      )}
      <div className="select-wrapper">
        {icon && <span className="select-icon">{icon}</span>}
        <select
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          required={required}
          disabled={disabled}
          className={`select ${icon ? 'select-with-icon' : ''} ${error ? 'select-error' : ''}`}
        >
          <option value="">{placeholder}</option>
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      {error && <span className="select-error-message">{error}</span>}
    </div>
  )
}

export default Select
