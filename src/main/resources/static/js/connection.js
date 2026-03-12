/* Connection to api */
let sseConnection = null;

/* Fetching of data*/
function connectToStream() {

    if (sseConnection) sseConnection.close();

    sseConnection = new EventSource('api/stream');

    // Standard message listener for updates
    sseConnection.onmessage = (event) => {
        let status = JSON.parse(event.data);
        console.log("Status Update:", status);

        // Update
        updateMapUIDeparture(status.departingTrains);
        updateMapUIArrival(status.upcomingTrains);
        updateDensityAndCount(status);

        document.getElementsByClassName("spinner_icon")[0].style.display = "none";
    };

    sseConnection.onerror = (err) => {
        // 2 means the connection is permanently closed
        if (err.target.readyState === EventSource.CLOSED) {
            console.error("SSE Connection was closed by the server.");
        } else if (err.target.readyState === EventSource.CONNECTING) {
            console.log("SSE is attempting to reconnect...");
        } else {
            console.error("An SSE error occurred.", err);
        }
        sseConnection.close();
    };
}