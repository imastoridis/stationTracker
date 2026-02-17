/* Connection to api */
let sseConnection = null;

/* Fetching of data*/
function connectToStream() {

    if (sseConnection) sseConnection.close();

    sseConnection = new EventSource('/api/stream');

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