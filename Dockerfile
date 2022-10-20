FROM maven:3-openjdk-11 AS build
LABEL description="Wire Texas Holdem bot"
LABEL project="wire-bots:holdem"

WORKDIR /app

# download dependencies
COPY pom.xml ./
RUN mvn verify --fail-never -U

# build
COPY . ./
RUN mvn -Dmaven.test.skip=true package

# runtime stage
FROM wirebot/runtime:1.2.0

RUN mkdir /opt/holdem
RUN mkdir /opt/holdem/cards
RUN mkdir /etc/holdem

COPY --from=build /app/cards/* /opt/holdem/cards/

WORKDIR /opt/holdem

EXPOSE  8080 8081

# Copy configuration
COPY conf/holdem.yaml /etc/holdem/

# Copy built target
COPY --from=build /app/target/holdem.jar /opt/holdem/

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=/opt/holdem/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH

EXPOSE  8080 8081
ENTRYPOINT ["java", "-jar", "holdem.jar", "server", "/etc/holdem/holdem.yaml"]
