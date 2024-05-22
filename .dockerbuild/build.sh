#!/usr/bin/env bash

set -e # Exit the script as soon as any statement returns a non-zero exit code.

SCRIPT=$(readlink -f $0)
SCRIPT_PATH=$(dirname "$SCRIPT")

# Move to script dir
cd "${SCRIPT_PATH}"

# Read env file
[ -f ./env ] && . ./env

## Variables definition
# APP_REGISTRY_IMAGE = harbor.solumesl.com/solum/aims-client:1.2.3.4-DEV-common
# APP_REGISTRY = harbor.solumesl.com
# APP_NAME = aims-client
# TAG_VERSION = 1.2.3.4
# TAG_TIER = DEV
# TAG_CUSTOMER = common

#if [ -z "${CI_REGISTRY}" ]; then { echo >&2 "Please provide \$CI_REGISTRY 'registry.example.com' variable before running this script."; exit 1;} fi
#if [ -z "${CI_REGISTRY_IMAGE}" ]; then { echo >&2 "Please provide \$CI_REGISTRY_IMAGE 'registry.example.com/gitlab-org/gitlab-foss' variable before running this script."; exit 1;} fi

echo "CI Info"
echo "Docker Builder:       ${BUILDER:=docker}"
echo "Docker Registry:      ${CI_REGISTRY:=local}"
echo "Docker Registry User: ${CI_REGISTRY_USER:=local}"
echo "Docker Registry Pass: *****" && : "${CI_REGISTRY_PASSWORD:=local}"
echo "---"
echo "App Info"
echo "App Name:          ${APP_NAME:=aims-saas-adapter-local}"
echo "App Registry Name: ${APP_REGISTRY_IMAGE:=local}"
echo "App Maintainer:    ${APP_MAINTAINER:=local}"
echo "App Component:     ${APP_COMPONENT:=local}"
echo "App Part-Of:       ${APP_PART_OF:=local}"

# Get jar artifact
JAR_FILE_PATH=$(find "${SCRIPT_PATH}/../target/" -name *.jar | head -1)

# Get app registry harbor.solumesl.com from harbor.solumesl.com/project/image
APP_REGISTRY="$(cut -d '/' -f 1 <<<"$APP_REGISTRY_IMAGE")"

# read_pom_xml return variables
APP_ARTIFACT=""
APP_VERSION=""
APP_PATCH_VERSION=""
APP_MINOR_VERSION=""
APP_MAYOR_VERSION=""

read_pom_xml() {
    POM_XML_FILE_PATH=$(realpath "${SCRIPT_PATH}/../pom.xml")
    # POM_XML_FILE_PATH=$(realpath "pom.xml")
    [ -f "${POM_XML_FILE_PATH}" ] || {
        echo "pom.xml file not found '${POM_XML_FILE_PATH}'!"
        return 0
    }

    APP_ARTIFACT="$(cat "${POM_XML_FILE_PATH}" | xq -r '.project.artifactId')"
    APP_VERSION="$(cat "${POM_XML_FILE_PATH}" | xq -r '.project.version')"

    echo ""
    echo "Application Name: ${APP_ARTIFACT}"
    echo "Application Version: ${APP_VERSION}"
    echo "Application Patch Version: ${APP_PATCH_VERSION}"
    echo "Application Minor Version: ${APP_MINOR_VERSION}"
    echo "Application Mayor Version: ${APP_MAYOR_VERSION}"
    echo ""
}

docker_build() {
    # Description:
    #   Replace old variable value with new empty variable value
    #
    # Usage:
    #   replace_with_old_variable_value old_variable new_variable
    #
    # Example:
    #   DATABASE_URL="jdbc:postgresql://127.0.0.1:6010/AIMS_PORTAL_DB"
    #   AIMS_CLIENT_DATABASE_URL=
    #   replace_with_old_variable_value DATABASE_URL AIMS_CLIENT_DATABASE_URL
    #
    ${BUILDER} build --network host \
        --no-cache \
        --pull \
        --build-arg "SOURCE_REGISTRY_IMAGE=${SOURCE_REGISTRY_IMAGE}" \
        --build-arg "BUILD_REGISTRY_IMAGE=${BUILD_REGISTRY_IMAGE}" \
        --build-arg "APP_REGISTRY_IMAGE=${APP_REGISTRY_IMAGE}" \
        --build-arg "MAINTAINER=${APP_MAINTAINER}" \
        --build-arg "NAME=${APP_NAME}" \
        --build-arg "COMPONENT=${APP_COMPONENT}" \
        --build-arg "PART_OF=${APP_PART_OF}" \
        --build-arg "VERSION=${APP_VERSION}" \
        --build-arg "CI_COMMIT_AUTHOR=${CI_COMMIT_AUTHOR}" \
        --build-arg "CI_COMMIT_SHORT_SHA=${CI_COMMIT_SHORT_SHA}" \
        -t "${APP_REGISTRY_IMAGE}:${APP_VERSION}" \
        -f "${SCRIPT_PATH}/Dockerfile" ..
}

docker_push() {
    # Check if tag exists then push.
    [ ! -z "${APP_VERSION}" ] && ${BUILDER} push "${APP_REGISTRY_IMAGE}:${APP_VERSION}"
}

main() {

    echo " "
    echo "----------------------------------------------------------"
    echo "Starting build.sh script"
    echo "----------------------------------------------------------"
    echo " "

    read_pom_xml

    if [ "$CI_REGISTRY" = "local" ]; then
        echo "Skipping ${BUILDER} login..."
    else
        echo "${CI_REGISTRY_PASSWORD}" | ${BUILDER} login --username "${CI_REGISTRY_USER}" --password-stdin "${CI_REGISTRY}"
    fi
    docker_build
    docker_push

    echo " "
    echo "----------------------------------------------------------"
    echo "Finished."

}

main
