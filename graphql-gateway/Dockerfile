FROM openjdk:8
ADD target/scala-2.12/graphql-gateway-assembly-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT exec java -cp /app.jar GraphqlGateway