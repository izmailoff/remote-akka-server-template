#!/bin/bash

export AKKA_KEYSTORE='.'

java -Xms128M -Xmx1200M -Xss10M -XX:MaxPermSize=512m -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"
