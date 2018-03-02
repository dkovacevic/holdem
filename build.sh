#!/usr/bin/env bash
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t dejankovacevic/holdem-bot:latest .
docker push dejankovacevic/holdem-bot
