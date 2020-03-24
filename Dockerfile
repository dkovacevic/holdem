FROM dejankovacevic/bots.runtime:2.10.3

COPY target/holdem.jar /opt/holdem/holdem.jar
COPY conf/holdem.yaml  /etc/holdem/holdem.yaml
COPY cards             /opt/holdem/cards

WORKDIR /opt/holdem

EXPOSE  8080 8081 8082

ENTRYPOINT ["java", "-jar", "holdem.jar", "server", "/etc/holdem/holdem.yaml"]