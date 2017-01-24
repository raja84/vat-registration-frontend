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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Result
import services.VatRegistrationService
import play.api.test.Helpers._

import scala.concurrent.Future

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]
  implicit val materializer = app.materializer

  object TestSummaryController extends SummaryController(mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  s"GET ${routes.SummaryController.show()}" should {
    "return HTML" in {
      when(mockVatRegistrationService.getRegistrationSummary()(Matchers.any())).thenReturn(Future.successful(Some(validSummaryView)))

      callAuthorised(TestSummaryController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Check your answers")
      }
    }
  }

  s"GET ${routes.SummaryController.show()}" should {
    "return internal server error" in {
      when(mockVatRegistrationService.getRegistrationSummary()(Matchers.any())).thenReturn(Future.successful(None))

      callAuthorised(TestSummaryController.show, mockAuthConnector) {
        (response: Future[Result]) =>
          status(response) mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

}