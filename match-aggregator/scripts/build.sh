#!/usr/bin/env bash

IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_REPOSITORY=${IMAGE_REPOSITORY:-quay.io/evanshortiss/shipwars-streams-match-aggregates}
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

mvn clean install
docker build . -f src/main/docker/Dockerfile.jvm -t ${IMAGE_REPOSITORY}:${IMAGE_TAG}
