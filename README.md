# book-shelf

This sample project shows concept how to integrate Lagom/Akka microservices and Zio ecosystem.

It consists of two parts:
 - graphql-gateway - uses Caliban to serve graphql requests
 - book-service - CQRS/ES Lagom microservice that's built as pure FP service on the top of ZIO.
 - recommendation-service - Lagom microservice for fulltext search built on top of ZIO and elastic4s.
 
Synchronous communication between services performed by zio-grpc library.
