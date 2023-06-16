/*
 * Copyright 2020 Kirill5k
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mongo4cats.zio.json

import mongo4cats.bson.json._
import mongo4cats.bson.{BsonValue, Document, ObjectId}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import zio.json._
import zio.json.ast.Json

import java.time.{Instant, LocalDate}
import java.util.UUID

class MongoJsonCodecsSpec extends AnyWordSpec with Matchers with MongoJsonCodecs {

  "ObjectId codec" should {
    "encode and decode ObjectId to json and back" in {
      val oid  = ObjectId.gen
      val json = s"""{"${Tag.id}":"${oid.toHexString}"}"""

      objectIdEncoder.encodeJson(oid).toString mustBe json
      objectIdDecoder.decodeJson(json) mustBe Right(oid)
    }
  }

  "Instant codec" should {
    "encode and decode Instant to json and back" in {
      val inst = Instant.now()
      val json = s"""{"${Tag.date}":"$inst"}"""

      instantEncoder.encodeJson(inst).toString mustBe json
      instantDecoder.decodeJson(json) mustBe Right(inst)
    }
  }

  "LocalDate codec" should {
    "encode and decode LocalDate to json and back" in {
      val date = LocalDate.now()
      val json = s"""{"${Tag.date}":"${date}"}"""

      localDateEncoder.encodeJson(date).toString mustBe json
      localDateDecoder.decodeJson(json) mustBe Right(date)
    }
  }

  "UUID codec" should {
    "encode and decode UUID to json and back" in {
      val uuid = UUID.fromString("cfbca728-4e39-4613-96bc-f920b5c37e16")
      val json = s"""{"${Tag.binary}":{"base64":"z7ynKE45RhOWvPkgtcN+Fg==","subType":"04"}}"""

      uuidEncoder.encodeJson(uuid).toString mustBe json
      uuidDecoder.decodeJson(json) mustBe Right(uuid)
    }
  }

  "Document codec" should {
    "encode and decode Document to json and back" in {
      val id = ObjectId.gen
      val ts = Instant.now
      val document = Document(
        "_id"           -> BsonValue.objectId(id),
        "string"        -> BsonValue.string("string"),
        "null"          -> BsonValue.Null,
        "boolean"       -> BsonValue.True,
        "long"          -> BsonValue.long(ts.toEpochMilli),
        "int"           -> BsonValue.int(1),
        "bigDecimal"    -> BsonValue.bigDecimal(BigDecimal(100.0)),
        "array"         -> BsonValue.array(BsonValue.string("a"), BsonValue.string("b")),
        "dateInstant"   -> BsonValue.instant(ts),
        "dateEpoch"     -> BsonValue.instant(ts),
        "dateLocalDate" -> BsonValue.instant(Instant.parse("2022-01-01T00:00:00Z")),
        "document"      -> BsonValue.document(Document("field1" -> BsonValue.string("1"), "field2" -> BsonValue.int(2))),
        "uuid"          -> BsonValue.uuid(UUID.fromString("cfbca728-4e39-4613-96bc-f920b5c37e16"))
      )

      val json =
        s"""{
          |  "_id" : {
          |    "${Tag.id}" : "${id.toHexString}"
          |  },
          |  "string" : "string",
          |  "null" : null,
          |  "boolean" : true,
          |  "long" : ${ts.toEpochMilli},
          |  "int" : 1,
          |  "bigDecimal" : 100.0,
          |  "array" : [
          |    "a",
          |    "b"
          |  ],
          |  "dateInstant" : {
          |    "${Tag.date}" : "$ts"
          |  },
          |  "dateEpoch" : {
          |    "${Tag.date}" : "$ts"
          |  },
          |  "dateLocalDate" : {
          |    "${Tag.date}" : "2022-01-01T00:00:00Z"
          |  },
          |  "document" : {
          |    "field1" : "1",
          |    "field2" : 2
          |  },
          |  "uuid" : {
          |    "${Tag.binary}" : {
          |      "base64" : "z7ynKE45RhOWvPkgtcN+Fg==",
          |      "subType" : "04"
          |    }
          |  }
          |}""".stripMargin

      Json.decoder.decodeJson(documentEncoder.encodeJson(document)).toOption.get.toJsonPretty mustBe json
      documentDecoder.decodeJson(json) mustBe Right(document)
    }
  }
}
