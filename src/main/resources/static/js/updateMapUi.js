/* Add date */
let renderClock = function (){
    document.getElementById("date").innerText = moment().format("Do MMM YYYY, h:mm:ss a");
}
renderClock()
setInterval(renderClock, 1000)


/**
 * Map update
 */

/* Update density*/
function updateDensityAndCount(status){
    const colors = {
        'LOW': '#28a745',      // Green
        'MEDIUM': '#ffc107',   // Yellow
        'HIGH': '#fd7e14',     // Orange
        'CRITICAL': '#dc3545'  // Red
    };
    const newColor = colors[status.densityLevel] || '#6c757d';

    /* Update updated_at*/
    document.getElementById('updated-at').innerText = `Updated at : ${moment().format(" h:mm:ss a")}`;

    /* Update density*/
    document.getElementById('density-val').innerText = status.densityLevel;
    document.getElementById('density-val').style.color  = newColor;
    document.getElementById('density-val-icon').style.color = newColor

    /* Update train count*/
    document.getElementById('train-count').innerText = status.trainCount;
}

/**
* Update train arrivals and departures
*/
var trainIcon = L.icon({
    iconUrl: '/assets/train-solid-full.png',
    iconSize:     [38, 95], // size of the icon
    shadowSize:   [50, 64], // size of the shadow
    /* iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
     shadowAnchor: [4, 62],  // the same for the shadow
     popupAnchor:  [-3, -76] // point from which the popup should open relative to the iconAnchor*/
});

/* Update arrivals*/
function updateMapUIArrival(upcomingTrains) {
    document.getElementById("arriving-trains").innerHTML = ''

    upcomingTrains.forEach(train => {
        // Update coords if exist
        if (train.latitude && train.longitude) {
            L.marker([train.latitude, train.longitude], {
                icon: trainIcon
            })
                .addTo(map)
                .bindPopup(`Train ${train.trainNumber} is here!`);
        }

        // Update template
        const delayHtml = train.delay !== 0
            ? `<em>Delay :</em> <span class="text_red">${train.delay} min</span>`
            : '';

        const timeHtml = train.delay !== 0
            ? `<em>Time :</em> <span class="text-decoration-line-through text_red">${ moment(train.arrivalTime).format("h:mm:ss a")}</span>
                <span class="text_green">  ${ moment(train.arrivalTime).add(train.delay, "minutes").format("h:mm:ss a")}</span>`
            : `<em>Arrival :</em> ${ moment(train.arrivalTime).format("h:mm:ss a")}`

        const templateHtml =   `
                <div class="d-flex flex-column">
                    <span><em>From :</em> <span class="fw-bold">${train.origin.replace(/ *\([^)]*\) */g, "")}</span></span>
                    <span><em>Train number :</em> ${train.trainNumber}</span>
                    <span>${timeHtml}</span>
                    <span>${delayHtml}</span>
                    <hr/>
                 </div>
            `;

        document.getElementById("arriving-trains").innerHTML += templateHtml
    })

}

/* Update departures*/
function updateMapUIDeparture(departingTrains) {
    departingTrains.forEach(train => {
        //Update coords
        if (train.latitude && train.longitude) {
            L.marker([train.latitude, train.longitude], {
                icon: trainIcon
            })
                .addTo(map)
                .bindPopup(`Train ${train.trainNumber} is here!`);
        }

        // Update template
        const delayHtmlDeparture = train.delay !== 0
            ? `<em>Delay :</em> <span class="text_red">${train.delay} min</span>`
            : '';

        const timeHtmlDeparture = train.delay !== 0
            ? `<em>Time :</em> <span class="text-decoration-line-through text_red">${ moment(train.departureTime).format("h:mm:ss a")}</span>
                <span class="text_green">  ${ moment(train.departureTime).add(train.delay, "minutes").format("h:mm:ss a")}</span>`
            : `<em>Departure :</em> ${ moment(train.departureTime).format("h:mm:ss a")}`

        const templateHtmlDeparture =   `
                <div class="d-flex flex-column">
                    <span><em>From :</em> <span class="fw-bold">${train.destination.replace(/ *\([^)]*\) */g, "")}</span></span>
                    <span><em>Train number :</em> ${train.trainNumber}</span>
                    <span>${timeHtmlDeparture}</span>
                    <span>${delayHtmlDeparture}</span>
                    <hr/>
                 </div>
            `;

        document.getElementById("departing-trains").innerHTML += templateHtmlDeparture
    })

}