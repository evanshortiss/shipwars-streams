#!/usr/bin/env bash

IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_REPOSITORY=${IMAGE_REPOSITORY:-quay.io/evanshortiss/shipwars-streams-shot-distribution}

docker push ${IMAGE_REPOSITORY}:${IMAGE_TAG}
