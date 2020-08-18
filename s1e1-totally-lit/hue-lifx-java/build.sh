#!/usr/bin/env bash

JAR="build/libs/hue-lifx-java-1.0-SNAPSHOT-all.jar"

if ! command -v java &> /dev/null
then
  # Echo to standard error since we need to use standard out to determine which command to run by other scripts
  >&2 echo "Java not detected, using Docker"
  >&2 echo "If you have not tried this before it may take a minute or two to build the container, subsequent runs will be faster"
  TAG="hue-lifx-java"
  >&2 docker build -t $TAG .
  echo "docker run --network=host --rm $TAG $JAR"
else
  >&2 echo "Java detected, building natively"
  >&2 ./gradlew build
  echo "java -cp $JAR"
fi
