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

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class HelloWorldControllerSpec extends SpecBase {

  "get" - {
    "returns and OK an text body of Hello World" in {

      val application = baseApplicationBuilder
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.HelloWorldController.get().url)
        val result  = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual "Hello World"
      }
    }

  }

}
