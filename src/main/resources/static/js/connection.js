/* Connection to api */
let sseConnection = null; // Global reference to manage the lifecycle

/* Fetching of data*/
function connectToStream() {

    if (sseConnection) sseConnection.close();
    sseConnection = new EventSource('/api/stream');

    // Standard message listener for updates
    sseConnection.onmessage = (event) => {
        const status = JSON.parse(event.data);
        console.log("Status Update:", status);

        //Update status
        updateDensityAndCount(status);

        //Update trains
        if (status.upcomingTrains != null){
            updateMapUIArrival(status.upcomingTrains);
        } else {
            updateMapUIDeparture(status.departingTrains);
        }
    };

    sseConnection.onerror = () => {
        console.error("SSE Connection failed.");
        sseConnection.close();
    };
}