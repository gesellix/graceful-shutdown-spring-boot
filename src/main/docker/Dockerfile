FROM openjdk:8u92-jre-alpine

# Allows to use the docker-client Java library with a unix socket.
# The Java based docker-client uses JNI which depends on libstdc++.so.6.
RUN apk add --no-cache libstdc++

ENV TZ Europe/Berlin

ENV SERVER_PORT 8080
EXPOSE $SERVER_PORT

RUN mkdir /app
WORKDIR /app

CMD ["java", "-Xms64m", "-Xmx64m", "-server", "-jar", "app.jar"]

COPY app.jar /app/
RUN ls
