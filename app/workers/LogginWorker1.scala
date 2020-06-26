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

import javax.inject.Inject
import models.EventWorkLog
import play.api.Logger

class LogginWorker1 @Inject()(
  mongoSource: MongoSource,
  loggingFlow: LoggingFlow
) {
  val logger = Logger(getClass)

  logger.info("MongoSource: " + mongoSource.hashCode() + " LoggingFlow: " + loggingFlow.hashCode())
  loggingFlow.tap(mongoSource(), logger, this.getClass.getName)
}
