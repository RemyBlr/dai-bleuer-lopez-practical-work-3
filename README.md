# Event Notifier
## Description

This is a simple event notifier that can be used for cities to send events to the server.
Clients can then access the server and get a list of all events or get more information about a specific event.

## Pre-requisites

- Java 17
- Docker

## Getting started
### Building the project
1. Clone the repository
```bash
git clone git@github.com:RemyBlr/dai-bleuer-lopez-practical-work-3.git
cd dai-bleuer-lopez-practical-work-3
```
2. Build the project
```bash
./mvnw package
```

## Running with docker
1. Build the docker image
```bash
docker-compose build
```
2. Run the docker container
```bash
docker-compose up
```
This will start the event emitter, the event server and the event client in isolated containers.


## Usage
### Event emitter
The event emitter is a simple program that will send events to the server. Either random or defined events.
You can run multiple instances of the event emitter to simulate multiple cities sending events to the server.
```bash
docker-compose run event-emitter-1
```
### Event server
The event server listens for multicast and unicast messages.
```bash
docker-compose run event-server
```
### Event client
he event client interacts with the event server to retrieve and display events.
```bash
docker-compose run event-client
```

## Docker compose
Docker Compose is used to manage the containers for the event emitter, event server, and event client.
The containers are connected to a custom network (events-network) to facilitate communication.