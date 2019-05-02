#!/bin/bash
if git config --get remote.origin.url 2>&1 | grep -F github.com/zhanhb/ckfinder-spring-boot -q && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  ./mvnw -s .travis/settings.xml "$@" deploy
else
  ./mvnw "$@" install
fi
