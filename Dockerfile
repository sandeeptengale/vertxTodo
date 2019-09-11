FROM java:8-jre

ENV VERTICLE_FILE build/libs/VetxDemo-1.0-SNAPSHOT.jar

# Set the location of the verticles
ENV VERTICLE_HOME C:\sandeep\hobby\backend

EXPOSE 8082

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY config_docker.json $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -jar VetxDemo-1.0-SNAPSHOT.jar -conf config_docker.json"]