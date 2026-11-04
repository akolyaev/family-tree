/**
 * Utility functions for Family Tree SPA.
 */

/**
 * Check if user is authenticated.
 */
export function isAuthenticated(): boolean {
  return sessionStorage.getItem('auth') === 'true';
}

/**
 * Set authentication state in sessionStorage.
 */
export function setAuthenticated(authenticated: boolean, username?: string, roles?: string[]): void {
  sessionStorage.setItem('auth', String(authenticated));
  if (username) sessionStorage.setItem('username', username);
  if (roles) sessionStorage.setItem('roles', JSON.stringify(roles));
}

/**
 * Get current username from sessionStorage.
 */
export function getUsername(): string | null {
  return sessionStorage.getItem('username');
}

/**
 * Get current roles from sessionStorage.
 */
export function getRoles(): string[] {
  const roles = sessionStorage.getItem('roles');
  return roles ? JSON.parse(roles) : [];
}

/**
 * Check if user has a specific role.
 */
export function hasRole(role: string): boolean {
  return getRoles().includes(role);
}

/**
 * Check if user can edit a person (owner or MASTER role).
 */
export function canEdit(personLogin: string | null): boolean {
  const username = getUsername();
  if (!username) return false;
  if (hasRole('MASTER')) return true;
  return personLogin === username;
}

/**
 * Clear auth state (logout).
 */
export function clearAuth(): void {
  sessionStorage.removeItem('auth');
  sessionStorage.removeItem('username');
  sessionStorage.removeItem('roles');
}

/**
 * Format a date for display.
 * Returns year only if full date is not available.
 */
export function formatDate(date: string | null): string {
  if (!date) return '—';
  // If it's already year-only (masked), return as-is
  if (/^\d{4}$/.test(date)) return date;
  // Parse ISO date and return year
  try {
    return new Date(date).getFullYear().toString();
  } catch {
    return date;
  }
}

/**
 * Get a placeholder image URL when photo is missing.
 */
export function getPlaceholderAvatar(): string {
  return 'data:image/svg+xml,' + encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" viewBox="0 0 80 80">
      <rect width="80" height="80" fill="#e0e0e0"/>
      <circle cx="40" cy="30" r="14" fill="#bdbdbd"/>
      <ellipse cx="40" cy="70" rx="24" ry="18" fill="#bdbdbd"/>
    </svg>
  `);
}
