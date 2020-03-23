package org.sigurdthor.book.lib

import java.net.URL
import java.time.Duration
import java.util.UUID

import play.api.libs.json.{JsValue, JsonValidationError, _}

import scala.util.Try

object JsonFormats {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = Reads {
    case JsString(s) =>
      try {
        JsSuccess(enum.withName(s).asInstanceOf[E#Value])
      } catch {
        case _: NoSuchElementException =>
          JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'")
      }
    case _ => JsError("String value expected")
  }
  def enumWrites[E <: Enumeration]: Writes[E#Value] = Writes(v => JsString(v.toString))

  def enumFormat[E <: Enumeration](enum: E): Format[E#Value] =
    Format(enumReads(enum), enumWrites)

  def singletonReads[O](singleton: O): Reads[O] = {
    (__ \ "value")
      .read[String]
      .collect(
        JsonValidationError(s"Expected a JSON object with a single field with key 'value' and value '${singleton.getClass.getSimpleName}'")
      ) {
        case s if s == singleton.getClass.getSimpleName => singleton
      }
  }

  def singletonWrites[O]: Writes[O] = Writes { singleton =>
    Json.obj("value" -> singleton.getClass.getSimpleName)
  }

  def singletonFormat[O](singleton: O): Format[O] =
    Format(singletonReads(singleton), singletonWrites)

  implicit val uuidReads: Reads[UUID] = implicitly[Reads[String]]
    .collect(JsonValidationError("Invalid UUID"))(Function.unlift { str =>
      Try(UUID.fromString(str)).toOption
    })
  implicit val uuidWrites: Writes[UUID] = Writes { uuid =>
    JsString(uuid.toString)
  }

  implicit val durationReads: Reads[Duration] = implicitly[Reads[String]]
    .collect(JsonValidationError("Invalid duration"))(Function.unlift { str =>
      Try(Duration.parse(str)).toOption
    })
  implicit val durationWrites: Writes[Duration] = Writes { duration =>
    JsString(duration.toString)
  }

  def eitherObjectFormat[A: Format, B: Format](leftKey: String, rightKey: String): Format[Either[A, B]] =
    OFormat(
      (__ \ rightKey).read[B].map(b => Right(b): Either[A, B]) orElse
        (__ \ leftKey).read[A].map(a => Left(a): Either[A, B]),
      OWrites[Either[A, B]] {
        case Right(rightValue) => Json.obj(rightKey -> Json.toJson(rightValue))
        case Left(leftValue)   => Json.obj(leftKey -> Json.toJson(leftValue))
      }
    )

  implicit val urlFormat: Format[URL] = new Format[URL] {
    override def reads(json: JsValue): JsResult[URL] =
      implicitly[Reads[String]].reads(json).flatMap { stringUrl =>
        Try(new URL(stringUrl)).map(JsSuccess(_)).getOrElse(JsError(s"invalid url $stringUrl"))
      }

    override def writes(url: URL): JsValue = JsString(url.toString)
  }
}
