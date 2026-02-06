
/* Init */
// Initialization of map at Lyon Part-Dieu
const lyonCoords = [45.76071664220606, 4.858701048532883];
const map = L.map('map').setView(lyonCoords, 15);

// OpenRailwayMap tiles
L.tileLayer('https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png', {
    attribution: '© OpenRailwayMap contributors'
}).addTo(map);

// A circle marker to display density
let stationMarker = L.circleMarker(lyonCoords, {
    radius: 20,
    fillColor: "#28a745", // Default Green (LOW)
    color: "#fff",
    weight: 2,
    opacity: 1,
    fillOpacity: 0.8
}).addTo(map).bindPopup("<b>Lyon Part-Dieu</b><br>Density: <span id='density-val'>LOW</span>");

/* Connection to api */
// Connection to the Spring Boot SSE Stream
const eventSource = new EventSource('/api/density/stream');

// update map on response
eventSource.onmessage = (event) => {
    const density = event.data;
    updateMapUI(density);
};

/* Map update */
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

    // Pulse effect if CRITICAL
    if (level === 'CRITICAL') {
        stationMarker.getElement().classList.add('pulse-animation');
    } else {
        stationMarker.getElement().classList.remove('pulse-animation');
    }
}

/* s the health of the system every 60 seconds*/
function checkSystemHealth() {
    fetch('/api/system/status')
        .then(response => response.json())
        .then(data => {
        console.log(data)
        const db_icon = document.getElementById('db-status-icon');
        const kafka_icon = document.getElementById('kafka-status-icon');
        const db_text = document.getElementById('db-status-text');
        const kafka_text = document.getElementById('kafka-status-text');

        if (data.database === 'UP') {
            db_icon.className = 'fa-solid fa-circle'; // Solid green-ish check
            db_icon.style.color = '#2ecc71';
            db_text.innerText = '';
        } else {
            db_icon.className = 'fa-solid fa-circle'; // Solid red-ish X
            db_icon.style.color = '#e74c3c';
            db_text.innerText = '';
        }

        if (data.kafka === 'UP') {
            kafka_icon.className = 'fa-solid fa-circle'; // Solid green-ish check
            kafka_icon.style.color = '#2ecc71';
            kafka_text.innerText = '';
        } else {
            kafka_icon.className = 'fa-solid fa-circle'; // Solid red-ish X
            kafka_icon.style.color = '#e74c3c';
            kafka_text.innerText = '';
        }
    })
        .catch(() => {
    });
}

// Check health every 60 seconds
setInterval(checkSystemHealth, 10000);
checkSystemHealth();