#!/bin/sh
#
#JAVA_OPTS=$JAVA_OPTIONS
#
APP_OPTS=""
#
echo "APP_OPTS=${APP_OPTS}"
#
exec java org.springframework.boot.loader.JarLauncher ${APP_OPTS}
