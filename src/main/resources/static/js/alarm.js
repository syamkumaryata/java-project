// Web Audio API tone generator and browser Notification manager (Section 10)
let audioCtx = null;
let activeOscillator = null;

export function initAudio() {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  return audioCtx.state === 'running';
}

export function playAlarmTone(durationSeconds = 15) {
  initAudio();
  if (!audioCtx) return;

  stopAlarmTone();

  const osc = audioCtx.createOscillator();
  const gain = audioCtx.createGain();

  osc.type = 'sine';
  osc.frequency.setValueAtTime(880, audioCtx.currentTime); // A5 tone
  // Pulsing beep pattern
  gain.gain.setValueAtTime(0.3, audioCtx.currentTime);

  osc.connect(gain);
  gain.connect(audioCtx.destination);

  osc.start();
  activeOscillator = osc;

  setTimeout(() => stopAlarmTone(), durationSeconds * 1000);
}

export function stopAlarmTone() {
  if (activeOscillator) {
    try {
      activeOscillator.stop();
      activeOscillator.disconnect();
    } catch (e) {}
    activeOscillator = null;
  }
}

export async function requestNotificationPermission() {
  if (!('Notification' in window)) return 'unsupported';
  return await Notification.requestPermission();
}

export function showSystemNotification(title, body) {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(title, {
      body,
      icon: '🚌'
    });
  }
}
