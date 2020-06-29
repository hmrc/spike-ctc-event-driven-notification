/*
 * Copyright 2020 HM Revenue & Customs
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

package workers

import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import javax.inject.Inject
import models.Event
import models.MongoCollection
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MongoSource @Inject()(mongo: ReactiveMongoApi)(implicit executionContext: ExecutionContext, mat: Materializer) {

  val selector = Json.obj(
    "locked" -> false,
  )

  val sort = Json.obj(
    "date" -> 1
  )

  val lockModifier =
    Json.obj(
      "$set" -> Json.obj(
        "locked" -> true
      )
    )

  def apply(): Source[Option[Event], Cancellable] =
    Source
      .tick(
        10.second,
        1.second,
        Source
          .fromIterator(
            () =>
              Iterator.single(
                mongo.database
                  .flatMap(
                    _.collection[JSONCollection](MongoCollection.eventsCollection)
                      .findAndUpdate(
                        selector = selector,
                        update = lockModifier,
                        fetchNewObject = true,
                        upsert = false,
                        sort = Some(sort),
                        fields = None,
                        bypassDocumentValidation = false,
                        writeConcern = WriteConcern.Default,
                        maxTime = None,
                        collation = None,
                        arrayFilters = Seq()
                      )
                      .map(_.result[Event])
                  )
            )
          )
          .map(x => Source.fromFuture(x))
          .flatMapConcat(identity)
      )
      .flatMapConcat(identity)

}
