#!/bin/bash
./gradlew clean build
sudo docker build --tag niledb/core .
