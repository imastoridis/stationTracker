// Initialize map at Lyon Part-Dieu
const lyonCoords = [45.7606, 4.8599];
const map = L.map('map').setView(lyonCoords, 15);

// Add OpenRailwayMap tiles
L.tileLayer('https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png', {
    attribution: '© OpenRailwayMap contributors'
}).addTo(map);

// Add a circle marker that we can change color
let stationMarker = L.circleMarker(lyonCoords, {
    radius: 20,
    fillColor: "#28a745", // Default Green (LOW)
    color: "#fff",
    weight: 2,
    opacity: 1,
    fillOpacity: 0.8
}).addTo(map).bindPopup("<b>Lyon Part-Dieu</b><br>Density: <span id='density-val'>LOW</span>");

// Connect to the Spring Boot SSE Stream
const eventSource = new EventSource('/api/density/stream');

eventSource.onmessage = (event) => {
    const density = event.data;
    updateMapUI(density);
};

function updateMapUI(level) {
    const colors = {
        'LOW': '#28a745',      // Green
        'MEDIUM': '#ffc107',   // Yellow
        'HIGH': '#fd7e14',     // Orange
        'CRITICAL': '#dc3545'  // Red
    };

    const newColor = colors[level] || '#6c757d';

    // Update marker style
    stationMarker.setStyle({ fillColor: newColor });
    document.getElementById('density-val').innerText = level;

    // Add a pulse effect if CRITICAL
    if (level === 'CRITICAL') {
        stationMarker.getElement().classList.add('pulse-animation');
    } else {
        stationMarker.getElement().classList.remove('pulse-animation');
    }
}

function checkSystemHealth() {
    fetch('/api/system/status')
        .then(response => response.json())
        .then(data => {
            document.getElementById('db-status').innerText = data.database === 'UP' ? '🟢' : '🔴';
            document.getElementById('kafka-status').innerText = data.kafka === 'UP' ? '🟢' : '🔴';
        })
        .catch(() => {
            document.getElementById('db-status').innerText = '🔴';
            document.getElementById('kafka-status').innerText = '🔴';
        });
}

// Check every 10 seconds
setInterval(checkSystemHealth, 10000);
checkSystemHealth(); // Initial check