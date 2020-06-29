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
import akka.stream.ActorAttributes
import akka.stream.Materializer
import akka.stream.Supervision
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import javax.inject.Inject
import models.Event
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class LoggingFlow @Inject()(
  mongo: ReactiveMongoApi
)(implicit executionContext: ExecutionContext, materializer: Materializer) {

  private val decider: Supervision.Decider = {
    case NonFatal(_) => Supervision.resume
    case _           => Supervision.stop
  }

  def tap(source: Source[Option[Event], Cancellable], logger: Logger, workerName: String): Cancellable =
    source
      .map {
        case Some(Event(info, _, _)) => logger.info(s"${logger.logger.getName} Notification for info: $info") // HTTP notification
        case None                    => logger.error("No notifications")
      }
      .toMat(Sink.ignore)(Keep.left)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .run()

}
