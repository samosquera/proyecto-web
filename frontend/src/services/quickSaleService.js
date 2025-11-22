import api from './api'

const quickSaleService = {
  // Create quick sale (CLERK, DRIVER, ADMIN)
  createQuickSale: async (data) => {
    const response = await api.post('/api/v1/quick-sale/create', data)
    return response.data
  },

  // Get available seats for quick sale
  getAvailableSeats: async (tripId) => {
    const response = await api.get(`/api/v1/quick-sale/available/${tripId}`)
    return response.data
  },
}

export default quickSaleService
