import api from './api'

const configService = {
  // Get all configs (ADMIN)
  getAllConfigs: async () => {
    const response = await api.get('/api/v1/configs/all')
    return response.data
  },

  // Get config by ID (ADMIN)
  getConfigById: async (id) => {
    const response = await api.get(`/api/v1/configs/${id}`)
    return response.data
  },

  // Get config by key (ADMIN)
  getConfigByKey: async (key) => {
    const response = await api.get(`/api/v1/configs/key/${key}`)
    return response.data
  },

  // Get config value by key (ADMIN)
  getConfigValue: async (key) => {
    const response = await api.get(`/api/v1/configs/key/${key}/value`)
    return response.data
  },

  // Get configs by category (ADMIN)
  getConfigsByCategory: async (category) => {
    const response = await api.get(`/api/v1/configs/category/${category}`)
    return response.data
  },

  // Create config (ADMIN)
  createConfig: async (data) => {
    const response = await api.post('/api/v1/configs/create', data)
    return response.data
  },

  // Update config (ADMIN)
  updateConfig: async (id, data) => {
    const response = await api.put(`/api/v1/configs/update/${id}`, data)
    return response.data
  },

  // Delete config (ADMIN)
  deleteConfig: async (id) => {
    await api.delete(`/api/v1/configs/delete/${id}`)
  },

  // Common config keys
  CONFIG_KEYS: {
    BAGGAGE_FREE_WEIGHT_KG: 'baggage.free.weight.kg',
    BAGGAGE_EXTRA_PRICE_PER_KG: 'baggage.extra.price.per.kg',
    CANCELLATION_FULL_REFUND_HOURS: 'cancellation.full.refund.hours',
    CANCELLATION_PARTIAL_REFUND_HOURS: 'cancellation.partial.refund.hours',
    CANCELLATION_PARTIAL_REFUND_PERCENTAGE: 'cancellation.partial.refund.percentage',
    OVERBOOKING_PERCENTAGE: 'overbooking.percentage',
    OVERBOOKING_MAX_PERCENTAGE: 'overbooking.max.percentage',
    OVERBOOKING_REQUIRES_APPROVAL: 'overbooking.requires.approval',
  },
}

export default configService
