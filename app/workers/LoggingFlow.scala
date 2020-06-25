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
import akka.stream.ActorAttributes
import akka.stream.Materializer
import akka.stream.Supervision
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import javax.inject.Inject
import models.Lock
import models.MongoCollection
import models.TestData
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.LastError
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

class LoggingFlow @Inject()(
  mongo: ReactiveMongoApi
)(implicit executionContext: ExecutionContext, materializer: Materializer) {

  private val documentExistsErrorCode = Some(11000)

  private def lockCollection =
    mongo.database.map(_.collection[JSONCollection](MongoCollection.eventsLockCollection))

  private def addLock(lock: Lock, logger: Logger): Future[Option[Unit]] =
    lockCollection
      .flatMap(
        _.insert(false)
          .one[Lock](lock)
          .map(_ => Some(()))
          .recover {
            case err: LastError if err.code == documentExistsErrorCode =>
              None
          }
      )

  private val decider: Supervision.Decider = {
    case NonFatal(_) => Supervision.resume
    case _           => Supervision.stop
  }

  def tap(source: Source[TestData, Future[NotUsed]], logger: Logger, lockFn: String => Lock): Future[NotUsed] = {
    logger.info(s"Started")

    source
      .mapAsync(1)({
        case t @ TestData(a, _) =>
          val lock = lockFn(a)
          addLock(lock, logger)
            .map {
              case Some(_) => logger.info(s"${logger.logger}: a=${t.a}")
              case None    => logger.info(s"${logger.logger}: Other worker got there first")
            }
      })
      .toMat(Sink.ignore)(Keep.left)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .run()
  }

}
