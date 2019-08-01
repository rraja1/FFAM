#! /bin/bash

#set -e

docker_command='docker-compose -f ffam.yaml'

if [[ $1 == "start" ]]; then
  $docker_command down --remove-orphans
  $docker_command build
  $docker_command up
fi

if [[ $1 == "stop" ]]; then
  $docker_command down --remove-orphans
fi

