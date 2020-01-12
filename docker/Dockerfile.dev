#FROM openjdk:8-alpine
FROM adoptopenjdk/openjdk8-openj9:alpine-slim
COPY torrssen2-*.jar torrssen2.jar
#COPY run.sh /run.sh
#COPY kill.sh /kill.sh
#RUN apk --no-cache add git
#RUN git clone https://github.com/tarpha/torrssen2.git
VOLUME [ "/root/data" ]
EXPOSE 8080
#ENV BASE_URL http://localhost:8080
CMD [ "java", "-jar", "torrssen2.jar", "--spring.profiles.active=dev"]
#ENTRYPOINT ["/run.sh"]