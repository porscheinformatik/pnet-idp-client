#!/bin/bash

IMAGE_PREFIX="$1"
CLIENT_ID="$2"
CLIENT_SECRET="$3"

if [[ -z ${IMAGE_PREFIX} ]]
then
  echo 'Image Name Prefix:'
  read IMAGE_PREFIX
fi

if [[ -z ${CLIENT_ID} ]]
then
  echo 'Client ID:'
  read CLIENT_ID
fi

if [[ -z ${CLIENT_SECRET} ]]
then
  echo 'Client Secret:'
  read CLIENT_SECRET
fi


J_OPTS="-Dspring.profiles.active=local,docker"
J_OPTS+=" -Doidc.client.id=$CLIENT_ID"
J_OPTS+=" -Doidc.client.secret=$CLIENT_SECRET"

CONTAINER="pnet-idp-client-showcase"

echo "Stop container $CONTAINER (if still running)..."
docker stop $CONTAINER 2>/dev/null

echo "Run container $CONTAINER..."
docker run --rm -p 8080:8080 --name=$CONTAINER -e JAVA_OPTS="$J_OPTS" $IMAGE_PREFIX/pnet/$CONTAINER:$USERNAME
