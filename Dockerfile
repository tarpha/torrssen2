FROM node:current-alpine
ENV NODE_OPTIONS --openssl-legacy-provider
RUN mkdir /torrssen2
COPY . /torrssen2
WORKDIR /torrssen2/nuxt
#RUN apt install -y yarn
RUN yarn && yarn build --spa
#RUN npm install && npm run build -- --spa
RUN mkdir -p ../src/main/resources/static
RUN cp -R dist/* ../src/main/resources/static

FROM gradle:jdk8
RUN mkdir /torrssen2
WORKDIR /torrssen2
COPY --from=0 /torrssen2 .
RUN gradle bootjar

#FROM adoptopenjdk/openjdk8-openj9:alpine-slim
#FROM adoptopenjdk:8-openj9-focal
FROM openjdk:8-jre
COPY --from=1 /torrssen2/build/libs/torrssen2-*.jar torrssen2.jar
VOLUME [ "/root/data" ]
EXPOSE 8080
#CMD [ "java", "-Xshareclasses", "-Xquickstart", "-jar", "torrssen2.jar"]
CMD [ "java", "-jar", "torrssen2.jar"]
