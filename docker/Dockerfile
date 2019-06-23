FROM openjdk:8-alpine
#COPY torrssen2-0.7.6.jar torrssen2.jar
COPY run.sh /run.sh
RUN apk --no-cache add git
RUN git clone https://github.com/tarpha/torrssen2.git
VOLUME [ "/root/data" ]
EXPOSE 8080
ENV BASE_URL http://localhost:8080
#CMD [ "java", "-jar", "torrssen2.jar"]
ENTRYPOINT ["/run.sh"]