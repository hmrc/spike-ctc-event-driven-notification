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

package controllers

import java.time.LocalDate
import java.time.LocalDateTime

import javax.inject.Inject
import models.MongoCollection
import models.TestData
import play.api.libs.json.Json
import play.api.libs.json.OFormat
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import play.api.mvc.DefaultActionBuilder
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class HelloWorldController @Inject()(
  cc: ControllerComponents,
  mongo: ReactiveMongoApi,
  defaultActionBuilder: DefaultActionBuilder
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def get(value: String): Action[AnyContent] = defaultActionBuilder.async {

    val newTestData = TestData(value, LocalDateTime.now())

    insert(newTestData).map {
      _ =>
        Created(s"Created new record for `${newTestData.a}` with timestamp `${newTestData.date}``")
    }
  }

  private def insert(data: TestData): Future[WriteResult] = {
    import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter

    def collection: Future[JSONCollection] =
      mongo.database.map(_.collection[JSONCollection](MongoCollection.eventsCollection))

    val jsonData = Json.toJsObject(data)

    collection.flatMap(_.insert(false).one(jsonData))
  }

}
