FROM node:current-alpine
RUN mkdir /torrssen2
COPY . /torrssen2
WORKDIR /torrssen2/nuxt
RUN npm install && npm run build -- --spa
RUN [ ! -d ../src/main/resources/static ] && mkdir -p ../src/main/resources/static
RUN rm -rf ../src/main/resources/static/*
RUN cp -R dist/* ../src/main/resources/static

FROM gradle:6.5.0-jdk8
RUN mkdir /torrssen2
WORKDIR /torrssen2
COPY --from=0 /torrssen2 .
RUN gradle bootjar

FROM adoptopenjdk/openjdk8-openj9:alpine-slim
COPY --from=1 /torrssen2/build/libs/torrssen2-*.jar torrssen2.jar
VOLUME [ "/root/data" ]
EXPOSE 8080
CMD [ "java", "-Xshareclasses", "-Xquickstart", "-jar", "torrssen2.jar"]
