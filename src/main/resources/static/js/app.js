// K2SO Watcher - Main JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    // Initialize all components
    initScanStatus();
    initAutoRefresh();
    initModals();
    initConfirmDialogs();
    initTooltips();
}

// Scan Status Polling
function initScanStatus() {
    const scanStatusElement = document.getElementById('scan-status');
    if (!scanStatusElement) return;

    // Check scan status every 3 seconds
    setInterval(checkScanStatus, 3000);
}

async function checkScanStatus() {
    try {
        const response = await fetch('/api/scan/status');
        const data = await response.json();

        updateScanUI(data);
    } catch (error) {
        console.error('Error checking scan status:', error);
    }
}

function updateScanUI(data) {
    const scanButton = document.getElementById('start-scan-btn');
    const scanStatus = document.getElementById('scan-status');

    if (data.scanInProgress) {
        if (scanButton) {
            scanButton.disabled = true;
            scanButton.innerHTML = '<span class="scan-spinner"></span> Scanning...';
        }
        if (scanStatus) {
            scanStatus.innerHTML = `
                <div class="scan-animation">
                    <div class="scan-spinner"></div>
                    <span class="scan-text">Network scan in progress...</span>
                </div>
            `;
            scanStatus.style.display = 'block';
        }
    } else {
        if (scanButton) {
            scanButton.disabled = false;
            scanButton.innerHTML = 'Start Scan';
        }
        if (scanStatus) {
            scanStatus.style.display = 'none';
        }
    }

    // Update stats if available
    if (data.devicesFound !== undefined) {
        updateStats(data);
    }
}

function updateStats(data) {
    const totalDevices = document.getElementById('total-devices');
    const onlineDevices = document.getElementById('online-devices');
    const unknownDevices = document.getElementById('unknown-devices');

    if (totalDevices) totalDevices.textContent = data.totalDevices || '0';
    if (onlineDevices) onlineDevices.textContent = data.onlineDevices || '0';
    if (unknownDevices) unknownDevices.textContent = data.unknownDevices || '0';
}

// Auto-refresh dashboard
function initAutoRefresh() {
    const dashboard = document.getElementById('dashboard');
    if (!dashboard) return;

    // Refresh stats every 30 seconds
    setInterval(refreshDashboardStats, 30000);
}

async function refreshDashboardStats() {
    try {
        const response = await fetch('/api/stats');
        const data = await response.json();
        updateStats(data);
    } catch (error) {
        console.error('Error refreshing stats:', error);
    }
}

// Start scan
async function startScan() {
    const button = document.getElementById('start-scan-btn');
    if (button) {
        button.disabled = true;
        button.innerHTML = '<span class="scan-spinner"></span> Starting...';
    }

    try {
        const response = await fetch('/api/scan/start', { method: 'POST' });
        const data = await response.json();

        if (data.success) {
            showNotification('Network scan started', 'success');
            checkScanStatus();
        } else {
            showNotification(data.message || 'Failed to start scan', 'error');
            if (button) {
                button.disabled = false;
                button.innerHTML = 'Start Scan';
            }
        }
    } catch (error) {
        console.error('Error starting scan:', error);
        showNotification('Error starting scan', 'error');
        if (button) {
            button.disabled = false;
            button.innerHTML = 'Start Scan';
        }
    }
}

// Modal handling
function initModals() {
    // Close modal on background click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });

    // Close modal on escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal.active').forEach(modal => {
                closeModal(modal.id);
            });
        }
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

// Confirmation dialogs
function initConfirmDialogs() {
    document.querySelectorAll('[data-confirm]').forEach(element => {
        element.addEventListener('click', function(e) {
            const message = this.getAttribute('data-confirm');
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });
}

// Tooltips
function initTooltips() {
    document.querySelectorAll('[data-tooltip]').forEach(element => {
        element.addEventListener('mouseenter', showTooltip);
        element.addEventListener('mouseleave', hideTooltip);
    });
}

function showTooltip(e) {
    const text = e.target.getAttribute('data-tooltip');
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: var(--surface);
        border: 1px solid var(--cyan);
        padding: 0.5rem 1rem;
        border-radius: 4px;
        font-size: 0.875rem;
        z-index: 10000;
        box-shadow: var(--glow-cyan);
    `;

    document.body.appendChild(tooltip);

    const rect = e.target.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.bottom + 10 + 'px';

    e.target._tooltip = tooltip;
}

function hideTooltip(e) {
    if (e.target._tooltip) {
        e.target._tooltip.remove();
        delete e.target._tooltip;
    }
}

// Notifications
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 100px;
        right: 20px;
        z-index: 10000;
        min-width: 300px;
        animation: slideIn 0.3s ease;
    `;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
}

// TOTP code input auto-focus
const totpInputs = document.querySelectorAll('.totp-input');
totpInputs.forEach((input, index) => {
    input.addEventListener('input', function() {
        if (this.value.length >= this.maxLength && totpInputs[index + 1]) {
            totpInputs[index + 1].focus();
        }
    });

    input.addEventListener('keydown', function(e) {
        if (e.key === 'Backspace' && this.value === '' && totpInputs[index - 1]) {
            totpInputs[index - 1].focus();
        }
    });
});

// Device type icon mapping
const deviceIcons = {
    'COMPUTER': 'desktop_windows',
    'LAPTOP': 'laptop',
    'SMARTPHONE': 'smartphone',
    'TABLET': 'tablet',
    'SMART_TV': 'tv',
    'GAMING_CONSOLE': 'sports_esports',
    'STREAMING_DEVICE': 'cast',
    'ROUTER': 'router',
    'SWITCH': 'device_hub',
    'ACCESS_POINT': 'wifi',
    'SMART_HOME': 'home',
    'PRINTER': 'print',
    'CAMERA': 'videocam',
    'SERVER': 'dns',
    'NAS': 'storage',
    'UNKNOWN': 'device_unknown'
};

function getDeviceIcon(deviceType) {
    return deviceIcons[deviceType] || deviceIcons['UNKNOWN'];
}

// Format timestamps
function formatTimestamp(timestamp) {
    if (!timestamp) return 'Never';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return Math.floor(diff / 60000) + ' min ago';
    if (diff < 86400000) return Math.floor(diff / 3600000) + ' hours ago';
    return date.toLocaleDateString();
}

// Update relative timestamps
function updateTimestamps() {
    document.querySelectorAll('[data-timestamp]').forEach(element => {
        const timestamp = element.getAttribute('data-timestamp');
        element.textContent = formatTimestamp(timestamp);
    });
}

// Update timestamps every minute
setInterval(updateTimestamps, 60000);

// CSS for animations (injected)
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Export functions for global use
window.startScan = startScan;
window.openModal = openModal;
window.closeModal = closeModal;
window.showNotification = showNotification;
