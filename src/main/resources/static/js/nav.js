// Sidebar and Mobile navigation component renderer
import { logout } from './auth.js';

export function renderNavigation(activePage, role = 'STUDENT') {
  const sidebar = document.getElementById('sidebar-container');
  const mobileNav = document.getElementById('mobile-nav-container');

  const studentLinks = [
    { id: 'dashboard', href: '/dashboard.html', label: 'Dashboard', icon: '🏠' },
    { id: 'tracking', href: '/tracking.html', label: 'Live Tracking', icon: '📍' },
    { id: 'route', href: '/route.html', label: 'My Route', icon: '🗺️' },
    { id: 'alerts', href: '/alerts.html', label: 'Arrival Alerts', icon: '🔔' },
    { id: 'alternatives', href: '/alternatives.html', label: 'Alternative Buses', icon: '🔄' },
    { id: 'profile', href: '/profile.html', label: 'Profile', icon: '👤' }
  ];

  const adminLinks = [
    { id: 'admin', href: '/admin.html', label: 'Admin Console', icon: '⚙️' },
    { id: 'profile', href: '/profile.html', label: 'Profile', icon: '👤' }
  ];

  const links = role === 'ADMIN' ? adminLinks : studentLinks;

  if (sidebar) {
    sidebar.innerHTML = `
      <div class="sidebar">
        <div class="brand">
          <div class="brand-logo">🚌</div>
          <div class="brand-text">
            <h1>Bus Tracker</h1>
            <p>SRM University-AP</p>
          </div>
        </div>
        <ul class="nav-links">
          ${links.map(l => `
            <li>
              <a href="${l.href}" class="nav-link ${l.id === activePage ? 'active' : ''}">
                <span>${l.icon}</span>
                <span>${l.label}</span>
              </a>
            </li>
          `).join('')}
          <li style="margin-top: auto; padding-top: 1rem;">
            <button id="btn-logout" class="nav-link btn-outline" style="width: 100%; border: none; cursor: pointer;">
              <span>🚪</span>
              <span>Sign Out</span>
            </button>
          </li>
        </ul>
      </div>
    `;
    document.getElementById('btn-logout')?.addEventListener('click', logout);
  }

  if (mobileNav) {
    mobileNav.innerHTML = `
      <nav class="mobile-nav">
        ${links.slice(0, 5).map(l => `
          <a href="${l.href}" class="mobile-nav-link ${l.id === activePage ? 'active' : ''}">
            <span style="font-size: 1.25rem;">${l.icon}</span>
            <span>${l.label}</span>
          </a>
        `).join('')}
      </nav>
    `;
  }
}
