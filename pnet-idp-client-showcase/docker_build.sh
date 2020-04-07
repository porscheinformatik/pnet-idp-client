#!/bin/bash

IMAGE_PREFIX="$1"
BASE_IMAGE="$2"

if [[ -z ${IMAGE_PREFIX} ]]
then
  echo 'Image Name Prefix:'
  read IMAGE_PREFIX
fi

if [[ -z ${BASE_IMAGE} ]]
then
  echo 'Base Image:'
  read BASE_IMAGE
fi

mvn -f ../pom.xml -am -pl pnet-idp-client-showcase install
mvn docker:build -Ddocker.tag=$USERNAME -Dimage.name.prefix=$IMAGE_PREFIX -Dimage.baseimage=$BASE_IMAGE
mvn docker:push -Ddocker.tag=$USERNAME
