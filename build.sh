#!/bin/bash
# This script builds two .war artifacts for production and test respectively.
#
# Prerequisites:
# * Make sure the user running the build can write in '/var/log/gazetteeer'.
# * Make sure that the `gazetteer-configs` directory is put next to the `gazetteer` repository directory.
#
# The results of this script will be copied to `./builds/<date the script was run>/`.

set -e

DIST_DIR=./builds
CONFIG_DIR=../gazetteer-configs

TODAY=$(date '+%Y-%m-%d')

mkdir -p $DIST_DIR/$TODAY

mkdir -p /var/log/gazetteer

cp $CONFIG_DIR/mail.properties src/main/webapp/WEB-INF/mail.properties

# Production build
cp $CONFIG_DIR/log4j2.xml.prod src/main/resources/log4j2.xml
cp $CONFIG_DIR/config.properties.prod src/main/webapp/WEB-INF/config.properties
cp $CONFIG_DIR/web.xml.prod src/main/webapp/WEB-INF/web.xml

mvn clean package
cp -r target/gazetteer.war $DIST_DIR/$TODAY/gazetteer.war

# Test build
cp $CONFIG_DIR/log4j2.xml.test src/main/resources/log4j2.xml
cp $CONFIG_DIR/config.properties.test src/main/webapp/WEB-INF/config.properties
cp $CONFIG_DIR/web.xml.test src/main/webapp/WEB-INF/web.xml

mvn clean package
cp -r target/gazetteer.war $DIST_DIR/$TODAY/gazetteer-test.war