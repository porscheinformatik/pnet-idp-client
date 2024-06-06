#!/bin/bash

IMAGE_NAME="$1"
CLIENT_ID="$2"
CLIENT_SECRET="$3"
ROUTE_URL="$4"
PROJECT="$5"

if [[ -z ${IMAGE_NAME} ]]
then
  read -p 'Image Name: ' IMAGE_NAME
fi

if [[ -z ${CLIENT_ID} ]]
then
  read -p 'Client ID: ' CLIENT_ID
fi

if [[ -z ${CLIENT_SECRET} ]]
then
  read -p 'Client Secret: ' CLIENT_SECRET
fi

if [[ -z ${ROUTE_URL} ]]
then
  read -p 'Route URL: ' ROUTE_URL
fi

if [[ -z ${PROJECT} ]]
then
  read -p 'Project: ' PROJECT
fi

read -r -d '' J_OPTS << EOM
    -Dspring.profiles.active=dev,docker
    -Doidc.client.id=$CLIENT_ID
    -Doidc.client.secret=$CLIENT_SECRET
EOM

oc project $PROJECT &>/dev/null

oc process -f oc_template.yml ENVIRONMENT=dev IMAGE_NAME="$IMAGE_NAME" ROUTE_URL="$ROUTE_URL" JAVA_OPTS="$J_OPTS" | oc apply -f -
oc import-image $PROJECT/pnet-idp-client-showcase-dev:latest
oc rollout status deployment/pnet-idp-client-showcase-dev
