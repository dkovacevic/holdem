FROM dejankovacevic/bots.runtime:2.10.0

COPY target/holdem.jar /opt/holdem/holdem.jar
COPY conf/holdem.yaml  /etc/holdem/holdem.yaml

WORKDIR /opt/holdem

