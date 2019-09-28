package graphql

case class Identity(user: String)

case class SecureContext(identity: Option[Identity], mutations: Mutations)
