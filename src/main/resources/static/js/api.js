// API communication wrapper with CSRF token support and 401 handling
let csrfToken = null;

export function setCsrfToken(token) {
  csrfToken = token;
}

export function getCsrfToken() {
  return csrfToken;
}

export async function apiRequest(endpoint, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';

  if (csrfToken && ['POST', 'PUT', 'DELETE', 'PATCH'].includes((options.method || 'GET').toUpperCase())) {
    headers['X-CSRF-Token'] = csrfToken;
  }

  const response = await fetch(endpoint, {
    ...options,
    headers
  });

  if (response.status === 401) {
    if (!window.location.pathname.endsWith('login.html')) {
      window.location.href = '/login.html?expired=true';
    }
  }

  if (!response.ok) {
    let errorData = {};
    try {
      errorData = await response.json();
    } catch (e) {
      errorData = { error: 'ERROR', message: response.statusText };
    }
    const error = new Error(errorData.message || 'API request failed');
    error.status = response.status;
    error.code = errorData.error;
    throw error;
  }

  return response.json();
}
