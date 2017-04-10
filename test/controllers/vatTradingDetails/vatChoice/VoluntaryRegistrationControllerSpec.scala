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

package controllers.vatTradingDetails.vatChoice

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class VoluntaryRegistrationControllerSpec extends VatRegSpec with VatRegistrationFixture {
  implicit val hc = HeaderCarrier()
  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVoluntaryRegistrationController extends VoluntaryRegistrationController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationController.show())

  s"GET ${routes.VoluntaryRegistrationController.show()}" should {

    "return HTML Voluntary Registration  page with no Selection" in {
      val voluntaryRegistration = VoluntaryRegistration("")

      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(voluntaryRegistration)))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> ""
      )) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you want to register voluntarily for VAT?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()
        (Matchers.eq(S4LKey[VoluntaryRegistration]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVoluntaryRegistrationController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you want to register voluntarily for VAT?")
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      when(mockS4LService.fetchAndGet[VoluntaryRegistration]()
        (Matchers.eq(S4LKey[VoluntaryRegistration]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestVoluntaryRegistrationController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("Do you want to register voluntarily for VAT?")
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe Status.BAD_REQUEST
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected Yes" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistration.yes)))

      when(mockS4LService.saveForm[VoluntaryRegistration](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_YES
      )) {
        response =>
          response redirectsTo s"$contextRoot/voluntary-registration-reason"
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected No" should {

    "redirect to the welcome page" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VoluntaryRegistration.no)))

      when(mockS4LService.clear()(any[HeaderCarrier]())).thenReturn(Future.successful(validHttpResponse))
      when(mockS4LService.saveForm[VoluntaryRegistration](any())(any(), any(), any()))
        .thenReturn(Future.successful(returnCacheMap))
      when(mockVatRegistrationService.deleteVatScheme()(any[HeaderCarrier]()))
        .thenReturn(Future.successful(true))

      AuthBuilder.submitWithAuthorisedUser(TestVoluntaryRegistrationController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_NO
      )) {
        response =>
          response redirectsTo contextRoot
      }
    }
  }

}