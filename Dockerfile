FROM dejankovacevic/bots.runtime:2.10.2

COPY target/holdem.jar /opt/holdem/holdem.jar
COPY conf/holdem.yaml  /etc/holdem/holdem.yaml

WORKDIR /opt/holdem

EXPOSE  8080 8081 8082
