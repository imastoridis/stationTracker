# Station Tracker: Lyon Part-Dieu

A real-time, event-driven Spring Boot application designed to monitor train arrivals and departures for Lyon Part-Dieu. 

The system calculates real-time station density and interpolates train coordinates between stations using the SNCF Open Data API.

## Testing the application

You can test the application live here: https://imastoridis.com/station-tracker/

## Technology stack

- Kafka
- Java 21
- Spring Boot
- Spring RestTemplate
- Spring WebSocket (STOMP)
- Maven
- Docker
- Jenkins
- PostgreSQL / Spring Data JPA
- LeafletJs

## Table of Contents

1. Architecture Overview
2. Key Features
3. Setup (Docker Deployment)

## 1./ Architecture Overview

The project follows a Producer-Consumer pattern using Apache Kafka.
### 1. The Data Ingestion (Producers)

   Generic Hierarchy: A base Producer<T, B> class handles the heavy lifting: SNCF Authentication, API fetching, and GPS interpolation for calculating live train positions.

   Coordinate Interpolation: Using the vehicle_journeys endpoint, the app calculates the approximate position of a train between two stops based on the current time and the scheduled stop times.

   Batching: Data is sent to Kafka as a BatchEvent to minimize network overhead.

### 2. The Processing Engine (Consumers)

   Consumers wait for all kafka tasks to complete before broadcasting updates to the UI.

   "Last One Out" Logic: When multiple Kafka topics (arrivals/departures) receive updates simultaneously, the system tracks active tasks. Only the final consumer to finish its database work triggers the WebSocket broadcast to the UI.

### 3. The Real-Time UI (WebSockets)

   STOMP/WebSocket: The StationStateService broadcasts a unified StationStatusUpdate object.

   Density Calculation: Automatically categorizes the station crowdedness (LOW, MEDIUM, HIGH, CRITICAL) based on the sum of active trains in a 30-minute window.

## 2./ Key Features

    ✅ Real-time GPS Tracking: Live interpolation of train positions.

    ✅ Synchronized State: Unified view of arrivals and departures.

    ✅ Scalable Design: Base classes allow easy addition of new stations or transport types.


## 3./ Setup (Docker Deployment)
The application is live, but if you want to run it locally you can follow these steps to run it with Docker.

### 1. Prerequisites 
- Docker and Docker Compose installed.
- An SNCF API Key ([Get one here](https://numerique.sncf.com/startup/api/token-developpeur/)).

### 2. Configuration. 

Create a .env file at the base of your project with the following variables: 

SNCF_API_KEY= YOUR_API_KEY_HERE

POSTGRES_USER=dev_user

POSTGRES_PASSWORD=dev_password

POSTGRES_DB=station_db

### 3. Run the application. 
Run the following command from the root of the project:

` docker-compose up`

The application will be available at http://localhost:8080/index.html

