/* s the health of the system every 60 seconds*/
function checkSystemHealth() {
    fetch('api/system/status')
        .then(response => response.json())
        .then(data => {
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