#!/usr/bin/env bash

echo "buildDocker now ...."
echo "===================="
#setup variables
APP_NAME="docker-demo"
APP_VERSION="1.0.$BUILD_NUMBER"
IMAGE_NAME="zhengne/$APP_NAME:$BUILD_NUMBER"
echo $IMAGE_NAME

docker build -t $IMAGE_NAME .
docker push $IMAGE_NAME

echo "===================="