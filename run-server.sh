#!/bin/sh

SPRING_PROFILES_ACTIVE=local ./gradlew  :server:bootrun|tee server.log

