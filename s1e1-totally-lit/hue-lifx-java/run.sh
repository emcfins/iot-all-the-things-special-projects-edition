#!/usr/bin/env bash

echo "If you have not tried this before it may take a minute or two to build the container, subsequent runs will be faster"
docker build -t hue-lifx-java .
docker run --network=bridge --rm hue-lifx-java
docker run --network=host --rm hue-lifx-java
