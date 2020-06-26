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

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import javax.inject.Inject
import models.MongoCollection
import models.Event
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.QueryOpts
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.akkastream.cursorProducer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class MongoSource @Inject()(mongo: ReactiveMongoApi)(implicit executionContext: ExecutionContext, mat: Materializer)
    extends (() => Source[Event, Future[NotUsed]]) {

  def apply(): Source[Event, Future[NotUsed]] =
    Source.fromFutureSource {
      mongo.database.map(
        _.collection[JSONCollection](MongoCollection.eventsCollection)
          .find(Json.obj(), None)
          .options(QueryOpts().tailable.awaitData)
          .cursor[Event]()
          .documentSource()
          .mapMaterializedValue(_ => NotUsed.notUsed())
      )
    }
}
