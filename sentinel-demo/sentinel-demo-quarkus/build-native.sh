#!/usr/bin/env bash
./mvnw package -X -Pnative \
-Dquarkus.native.graalvm-home=/opt/graalvm-ce-java8-20.0.0

#-Dquarkus.native.container-build=true \
#-Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-native-image:20.0.0-java11 
#sudo docker build -f src/main/docker/Dockerfile.native -t quarkus/sentinel-demo-quarkus .
#sudo docker run -i --rm -p 8080:8182 quarkus/sentinel-demo-quarkus


