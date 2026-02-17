// Define your base URLs
function getUrl(isProduction){
    const API_BASE_URL = isProduction
        ? '/stationTracker/'
        : '/';
    return API_BASE_URL
}
const API_BASE_URL = getUrl(isProduction)

/* Connection to api */
let sseConnection = null;

/* Fetching of data*/
function connectToStream() {

    if (sseConnection) sseConnection.close();
    // Determine environment based on the browser's current URL
    var isProduction = window.location.hostname === 'imastoridis.com';

    sseConnection = new EventSource(API_BASE_URL + 'api/stream');
    // Standard message listener for updates
    sseConnection.onmessage = (event) => {
        let status = JSON.parse(event.data);
        console.log("Status Update:", status);

        // Update
        updateMapUIDeparture(status.departingTrains);
        updateMapUIArrival(status.upcomingTrains);
        updateDensityAndCount(status);
    };

    sseConnection.onerror = () => {
        console.error("SSE Connection failed.");
        sseConnection.close();
    };
}