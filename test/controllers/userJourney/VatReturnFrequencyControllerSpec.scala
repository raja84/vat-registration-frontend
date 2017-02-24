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

import builders.AuthBuilder
import enums.CacheKeys
import fixtures.VatRegistrationFixture
import forms.vatDetails.VatReturnFrequencyForm
import helpers.VatRegSpec
import models.view.{AccountingPeriod, VatReturnFrequency, VoluntaryRegistration}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class VatReturnFrequencyControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object TestVatReturnFrequencyController extends VatReturnFrequencyController(mockS4LService, mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.VatReturnFrequencyController.show())

  s"GET ${routes.VatReturnFrequencyController.show()}" should {

    "return HTML when there's a Vat Return Frequency model in S4L" in {
      val vatReturnFrequency = VatReturnFrequency("")

      when(mockS4LService.fetchAndGet[VatReturnFrequency](Matchers.eq(CacheKeys.VatReturnFrequency.toString))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(vatReturnFrequency)))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.show(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> ""
      )){

        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("How often do you want to submit VAT Returns?")
      }
    }

    "return HTML when there's nothing in S4L" in {
      when(mockS4LService.fetchAndGet[VatReturnFrequency](Matchers.eq(CacheKeys.VatReturnFrequency.toString))
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[VatReturnFrequency]]()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestVatReturnFrequencyController.show, mockAuthConnector) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("How often do you want to submit VAT Returns?")
      }
    }
  }


  s"POST ${routes.VatReturnFrequencyController.submit()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
      )) {
        result =>
          status(result) mustBe  Status.BAD_REQUEST
      }

    }
  }

  s"POST ${routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Monthly" should {

    "return 303" in {
      val returnCacheMapVatReturnFrequency = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency.empty)))
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod.empty)))

      when(mockS4LService.saveForm[VatReturnFrequency]
        (Matchers.eq(CacheKeys.VatReturnFrequency.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[VatReturnFrequency]]()))
        .thenReturn(Future.successful(returnCacheMapVatReturnFrequency))

      when(mockS4LService.saveForm[AccountingPeriod]
        (Matchers.eq(CacheKeys.AccountingPeriod.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[AccountingPeriod]]()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.MONTHLY
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/summary"
      }

    }
  }

  s"POST ${routes.VatReturnFrequencyController.submit()} with Vat Return Frequency selected Quarterly" should {

    "return 303" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(VatReturnFrequency.empty)))
      val returnCacheMapAccountingPeriod = CacheMap("", Map("" -> Json.toJson(AccountingPeriod.empty)))

      when(mockS4LService.saveForm[VatReturnFrequency](Matchers.eq(CacheKeys.VatReturnFrequency.toString), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      when(mockS4LService.saveForm[AccountingPeriod]
        (Matchers.eq(CacheKeys.AccountingPeriod.toString), Matchers.any())
        (Matchers.any[HeaderCarrier](), Matchers.any[Format[AccountingPeriod]]()))
        .thenReturn(Future.successful(returnCacheMapAccountingPeriod))

      AuthBuilder.submitWithAuthorisedUser(TestVatReturnFrequencyController.submit(), mockAuthConnector, fakeRequest.withFormUrlEncodedBody(
        VatReturnFrequencyForm.RADIO_FREQUENCY -> VatReturnFrequency.QUARTERLY
      )) {
        response =>
          status(response) mustBe Status.SEE_OTHER
          redirectLocation(response).getOrElse("") mustBe  "/vat-registration/accounting-period"
      }

    }
  }

}