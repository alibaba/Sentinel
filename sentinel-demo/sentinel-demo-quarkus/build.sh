#!/usr/bin/env bash
mvn package
sudo docker build -f src/main/docker/Dockerfile.jvm -t quarkus/sentinel-demo-quarkus-jvm .
sudo docker run -i --rm -p 8080:8182 quarkus/sentinel-demo-quarkus-jvm
