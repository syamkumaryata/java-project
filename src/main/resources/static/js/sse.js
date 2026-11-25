// SSE Client with auto-reconnection and connection status badge
export class TrackingSseClient {
  constructor(options = {}) {
    this.eventSource = null;
    this.onLocationUpdate = options.onLocationUpdate || (() => {});
    this.onAlert = options.onAlert || (() => {});
    this.onStatusChange = options.onStatusChange || (() => {});
    this.reconnectTimeout = null;
  }

  connect() {
    this.onStatusChange('Connecting...');

    this.eventSource = new EventSource('/api/stream/tracking');

    this.eventSource.addEventListener('connected', (e) => {
      this.onStatusChange('Connected');
    });

    this.eventSource.addEventListener('bus-location', (e) => {
      const data = JSON.parse(e.data);
      this.onLocationUpdate(data);
    });

    this.eventSource.addEventListener('arrival-alert', (e) => {
      const data = JSON.parse(e.data);
      this.onAlert(data);
    });

    this.eventSource.onerror = () => {
      this.onStatusChange('Offline / Reconnecting');
      this.eventSource.close();
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = setTimeout(() => this.connect(), 4000);
    };
  }

  disconnect() {
    clearTimeout(this.reconnectTimeout);
    if (this.eventSource) {
      this.eventSource.close();
    }
  }
}
