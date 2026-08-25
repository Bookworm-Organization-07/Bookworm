/**
 * The backend models an admin as a single boolean on the user, which is
 * what /api/auth/login returns alongside the token.
 */
export function isAdminUser(user) {
  return !!user && user.admin === true
}
