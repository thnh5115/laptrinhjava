/**
 * Cookie Utility Functions
 * Client-side cookie management for JWT tokens
 */

export const cookies = {
  /**
   * Set a cookie
   * @param name Cookie name
   * @param value Cookie value
   * @param days Number of days until expiration (default: 7)
   */
  set: (name: string, value: string, days: number = 7) => {
    if (typeof window === 'undefined') return;
    
    const date = new Date();
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
    const expires = `expires=${date.toUTCString()}`;
    
    // Secure cookie settings
    // path=/ : Available on all pages
    // SameSite=Strict : CSRF protection
    // Secure flag will be added in production (HTTPS)
    const secure = process.env.NODE_ENV === 'production' ? 'Secure;' : '';
    
    document.cookie = `${name}=${value}; ${expires}; path=/; SameSite=Strict; ${secure}`;
  },

  /**
   * Get a cookie value by name
   * @param name Cookie name
   * @returns Cookie value or null if not found
   */
  get: (name: string): string | null => {
    if (typeof window === 'undefined') return null;
    
    const nameEQ = `${name}=`;
    const ca = document.cookie.split(';');
    
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) === ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    
    return null;
  },

  /**
   * Remove a cookie
   * @param name Cookie name
   */
  remove: (name: string) => {
    if (typeof window === 'undefined') return;
    
    // Set expiration to past date to delete cookie
    document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict;`;
  },
};

// Cookie names constants
export const COOKIE_NAMES = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
} as const;
