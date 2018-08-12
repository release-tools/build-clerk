#!/usr/bin/env bash
set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="$( cd ${SCRIPT_DIR}/../ && pwd )"
IMAGE_REPOSITORY="outofcoffee/"
DOCKER_LOGIN_ARGS=""

while getopts "e" OPT; do
    case ${OPT} in
        e) DOCKER_LOGIN_ARGS="--email dummy@example.com"
        ;;
    esac
done
shift $((OPTIND-1))

IMAGE_TAG="${1-dev}"

function login() {
    if [[ "dev" == "${IMAGE_TAG}" ]]; then
        echo -e "\nSkipped registry login"
    else
        echo -e "\nLogging in to Docker registry..."
        echo "${DOCKER_PASSWORD}" | docker login --username "${DOCKER_USERNAME}" --password-stdin ${DOCKER_LOGIN_ARGS}
    fi
}

function buildImage()
{
    IMAGE_NAME="$1"
    IMAGE_PATH="$2"
    FULL_IMAGE_NAME="${IMAGE_REPOSITORY}${IMAGE_NAME}:${IMAGE_TAG}"

    echo -e "\nBuilding Docker image: ${IMAGE_NAME}"
    cd "${ROOT_DIR}/${IMAGE_PATH}"
    docker build --tag "${FULL_IMAGE_NAME}" .
}

function pushImage()
{
    IMAGE_NAME="$1"
    FULL_IMAGE_NAME="${IMAGE_REPOSITORY}${IMAGE_NAME}:${IMAGE_TAG}"

    echo -e "\nPushing Docker image: ${IMAGE_NAME}"
    docker push "${FULL_IMAGE_NAME}"
}

function buildPushImage()
{
    IMAGE_NAME="$1"
    IMAGE_PATH="$2"
    echo -e "\nBuilding '${IMAGE_NAME}' image"

    buildImage "${IMAGE_NAME}" "${IMAGE_PATH}"

    if [[ "dev" == "${IMAGE_TAG}" ]]; then
        echo -e "\nSkipped pushing dev image"
    else
        pushImage "${IMAGE_NAME}"
    fi
}

login
buildPushImage build-clerk backend/server
