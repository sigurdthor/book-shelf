# book-shelf

This sample project shows concept how to integrate Lagom/Akka microservices and Zio ecosystem.
It consists of two parts: graphql-gateway that uses Caliban to serve graphql requests and book-service - CQRS/ES Lagom microservice
that is built as pure FP service on the top of ZIO.
Synchronous communication between services is performed by zio-grpc library.
