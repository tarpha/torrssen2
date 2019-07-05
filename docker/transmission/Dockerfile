FROM        alpine

ARG         DOCKER_UID

# Create a user to run the application
RUN         adduser -D -u ${DOCKER_UID} transmission
COPY        ./settings.json /home/transmission/.config/settings.json
RUN         chown -R transmission:transmission /home/transmission
WORKDIR     /home/transmission

# Data and config volumes
VOLUME      /home/transmission/.config
VOLUME      /home/transmission/Downloads
VOLUME      /home/transmission/incomplete
VOLUME      /home/transmission/watch

# Install Transmission
RUN         apk update && apk add --no-cache transmission-daemon

EXPOSE      9091

USER        transmission
ENTRYPOINT  ["transmission-daemon", "--foreground", "--log-info"]