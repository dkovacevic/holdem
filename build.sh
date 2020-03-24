#!/usr/bin/env bash
mvn package -DskipTests=true -Dmaven.javadoc.skip=true
docker build -t dejankovacevic/holdem-bot:1.3.0 .
docker push dejankovacevic/holdem-bot
kubectl delete pod -l name=holdem -n prod
kubectl get pods -l name=holdem -n prod

