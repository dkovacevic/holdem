FROM dejankovacevic/bots.runtime:latest

COPY target/holdem.jar /opt/holdem/holdem.jar
COPY conf/holdem.yaml  /etc/holdem/holdem.yaml

WORKDIR /opt/holdem

