/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.userJourney

import helpers.VatRegSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ApplicationSubmissionControllerSpec extends VatRegSpec {

  object TestApplicationSubmissionController extends ApplicationSubmissionController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ApplicationSubmissionController.show())



  s"GET ${routes.ApplicationSubmissionController.show()}" should {

    "display the submission confirmation page to the user" in {
      callAuthorised(TestApplicationSubmissionController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Application submitted")
      }
    }
  }

}