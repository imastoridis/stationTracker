// Initialization of map at Lyon Part-Dieu
const lyonCoords = [45.76071664220606, 4.858701048532883];
const map = L.map('map-container').setView(lyonCoords, 17);


/*45.760596
4.859409*/
// OpenRailwayMap tiles
L.tileLayer('https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png', {
    attribution: '© OpenRailwayMap contributors'
}).addTo(map);





/* Manual event trigger*/
async function triggerManualFetch() {
    try {
        const response = await fetch('/api/trigger-fetch/arrivals', {
            method: 'POST'
        });

        if (response.ok) {
            console.log("Backend fetch initiated!");
        }
    } catch (error) {
        console.error("Failed to trigger fetch:", error);
    }

    try {
        const response = await fetch('/api/trigger-fetch/departures', {
            method: 'POST'
        });

        if (response.ok) {
            console.log("Backend fetch initiated!");
        }
    } catch (error) {
        console.error("Failed to trigger fetch:", error);
    }
}

/* Trigger on page load*/
document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM fully loaded and parsed. Initializing SSE...");
    setTimeout(connectToStream, 1000);
});
