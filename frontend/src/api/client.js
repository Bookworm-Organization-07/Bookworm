import axios from 'axios'

const TOKEN_KEY = 'bookworm_token'
const REFRESH_TOKEN_KEY = 'bookworm_refresh_token'
const USER_KEY = 'bookworm_user'

const apiClient = axios.create({
  baseURL: '/api',
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Access tokens only last 15 minutes (JwtOptions.AccessTokenExpiryMinutes on
// the backend), so a 401 on an otherwise-normal request usually just means
// it expired mid-session, not that the user actually logged out. This swaps
// in a fresh access token using the refresh token (7 days,
// JwtOptions.RefreshTokenExpiryDays) via POST /auth/refresh, then retries
// the original request once. If several requests 401 at the same moment,
// they all share one in-flight refresh call instead of each firing their own.
let refreshInFlight = null

function refreshAccessToken() {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  if (!refreshToken) {
    return Promise.reject(new Error('No refresh token stored.'))
  }

  if (!refreshInFlight) {
    // Plain axios here, not apiClient - so if this call itself comes back
    // 401 (refresh token expired/invalid), it doesn't loop back through the
    // response interceptor below and try to refresh again.
    refreshInFlight = axios
      .post('/api/auth/refresh', { refreshToken })
      .then(({ data }) => {
        localStorage.setItem(TOKEN_KEY, data.token)
        localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken)
        return data.token
      })
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error
    // /auth/... calls (login, refresh itself, etc.) are excluded - a 401
    // from those means the credentials/refresh token were actually rejected,
    // not that an access token expired mid-session.
    const isAuthEndpoint = config?.url?.startsWith('/auth/')

    if (response?.status === 401 && !isAuthEndpoint && !config._retriedAfterRefresh) {
      config._retriedAfterRefresh = true
      try {
        const newAccessToken = await refreshAccessToken()
        config.headers.Authorization = `Bearer ${newAccessToken}`
        return apiClient(config)
      } catch {
        // Refresh token missing, expired, or rejected - there is no way to
        // silently recover, so clear the stored session and let the rest of
        // the app know it needs to send the user back to the login page.
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
        window.dispatchEvent(new Event('bookworm:session-expired'))
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
