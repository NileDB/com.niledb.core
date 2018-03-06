FROM openjdk:8-jre-alpine
#FROM java:8-jre

ENV VERTICLE_FILE com.niledb.core-0.8.2-fat.jar
ENV VERTICLE_HOME /usr/verticles

EXPOSE 80 80

COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/
COPY config-docker.json $VERTICLE_HOME/config.json

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE --conf config.json"]
