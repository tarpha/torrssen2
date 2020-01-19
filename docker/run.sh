#!/bin/sh

while true
do
  #cd /torrssen2 && git pull && cd /
  
  #cp /torrssen2/docker/torrssen2-*.jar torrssen2.jar
  
  #java -jar torrssen2.jar
  java $JAVA_OPTS -Xshareclasses -Xquickstart -jar torrssen2.jar
done
