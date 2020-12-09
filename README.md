# book-shelf

This sample project shows concept how to integrate Lagom/Akka microservices with Zio ecosystem.

It consists of three parts:
 - graphql-gateway - uses Caliban to serve graphql requests
 - book-service - CQRS/ES Lagom microservice that's built as pure FP service on the top of ZIO.
 - recommendation-service - Lagom microservice giving fulltext search capability to the project. It's built on the top of ZIO and elastic4s.
 
Synchronous communication between services is performed by zio-grpc library.
