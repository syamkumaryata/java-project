// Authentication state and user profile manager
import { apiRequest, setCsrfToken } from './api.js';

export async function checkSession() {
  try {
    const data = await apiRequest('/api/me');
    if (data.csrfToken) {
      setCsrfToken(data.csrfToken);
    }
    return data;
  } catch (err) {
    return null;
  }
}

export async function requireAuth(requiredRole = null) {
  const user = await checkSession();
  if (!user) {
    window.location.href = '/login.html';
    return null;
  }
  if (requiredRole && user.role !== requiredRole) {
    if (user.role === 'ADMIN') {
      window.location.href = '/admin.html';
    } else {
      window.location.href = '/dashboard.html';
    }
    return null;
  }
  return user;
}

export async function logout() {
  try {
    await apiRequest('/api/auth/logout', { method: 'POST' });
  } finally {
    window.location.href = '/login.html';
  }
}
