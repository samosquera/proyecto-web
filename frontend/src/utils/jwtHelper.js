/**
 * Utilidad para decodificar y trabajar con JWT tokens
 */

/**
 * Decodifica un JWT token sin verificar la firma
 * Solo para extraer información del payload en el cliente
 * NOTA: NUNCA usar esto para validación de seguridad
 */
export const decodeJWT = (token) => {
  try {
    if (!token) return null

    // Los JWT tienen 3 partes separadas por puntos: header.payload.signature
    const parts = token.split('.')
    if (parts.length !== 3) {
      console.warn('Token inválido: no tiene 3 partes')
      return null
    }

    // Decodificar el payload (segunda parte)
    const payload = parts[1]

    // Reemplazar caracteres URL-safe
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')

    // Decodificar base64
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )

    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Error al decodificar JWT:', error)
    return null
  }
}

/**
 * Extrae el role del usuario desde el JWT
 */
export const extractRoleFromToken = (token) => {
  const payload = decodeJWT(token)
  return payload?.role || null
}

/**
 * Extrae el userId desde el JWT
 */
export const extractUserIdFromToken = (token) => {
  const payload = decodeJWT(token)
  return payload?.userId || null
}

/**
 * Extrae el username desde el JWT
 */
export const extractUsernameFromToken = (token) => {
  const payload = decodeJWT(token)
  return payload?.sub || null // 'sub' es el subject (username/email)
}

/**
 * Verifica si el token ha expirado (basado en tiempo del cliente)
 * NOTA: Esto solo es una verificación preliminar, el servidor siempre debe validar
 */
export const isTokenExpired = (token) => {
  const payload = decodeJWT(token)
  if (!payload || !payload.exp) return true

  // exp está en segundos, Date.now() en milisegundos
  const expirationTime = payload.exp * 1000
  return Date.now() >= expirationTime
}

/**
 * Obtiene el tiempo restante hasta la expiración del token en segundos
 */
export const getTokenExpiryTime = (token) => {
  const payload = decodeJWT(token)
  if (!payload || !payload.exp) return 0

  const expirationTime = payload.exp * 1000
  const timeRemaining = Math.max(0, expirationTime - Date.now())
  return Math.floor(timeRemaining / 1000) // retornar en segundos
}

/**
 * Extrae todas las authorities/permissions del token
 */
export const extractAuthoritiesFromToken = (token) => {
  const payload = decodeJWT(token)
  return payload?.authorities || []
}

export default {
  decodeJWT,
  extractRoleFromToken,
  extractUserIdFromToken,
  extractUsernameFromToken,
  isTokenExpired,
  getTokenExpiryTime,
  extractAuthoritiesFromToken,
}
