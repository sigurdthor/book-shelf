FROM openjdk:8
ADD target/scala-2.12/graphql-gateway-assembly-1.0-SNAPSHOT.jar app.jar
ADD src/main/resources/grpc-debug-logging.properties grpc-debug-logging.properties
ENTRYPOINT exec java -Djava.util.logging.config.file=grpc-debug-logging.properties -cp /app.jar GraphqlGateway