#!/bin/sh

git clone https://github.com/tarpha/torrssen2.git
cp ./torrssen2/docker/torrssen2-*.jar torrssen2.jar

java -jar torrssen2.jar
