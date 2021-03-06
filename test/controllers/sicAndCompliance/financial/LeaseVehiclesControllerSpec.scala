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

package controllers.sicAndCompliance.financial

import builders.AuthBuilder
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.S4LKey
import models.view.sicAndCompliance.financial.LeaseVehicles
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.VatRegistrationService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class LeaseVehiclesControllerSpec extends VatRegSpec with VatRegistrationFixture {

  val mockVatRegistrationService = mock[VatRegistrationService]

  object LeaseVehiclesController extends LeaseVehiclesController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.LeaseVehiclesController.show())

  s"GET ${routes.LeaseVehiclesController.show()}" should {

    "return HTML when there's a Lease Vehicles or Equipment - model in S4L" in {
      val leaseVehicles = LeaseVehicles(true)

      when(mockS4LService.fetchAndGet[LeaseVehicles]()(any(), any(), any()))
        .thenReturn(Future.successful(Some(leaseVehicles)))

      AuthBuilder.submitWithAuthorisedUser(LeaseVehiclesController.show(), fakeRequest.withFormUrlEncodedBody(
        "leaseVehiclesRadio" -> ""
      )) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockS4LService.fetchAndGet[LeaseVehicles]()
        (Matchers.eq(S4LKey[LeaseVehicles]), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getVatScheme()(any()))
        .thenReturn(Future.successful(validVatScheme))

      callAuthorised(LeaseVehiclesController.show) {
        _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
      }
    }
  }

  "return HTML when there's nothing in S4L and vatScheme contains no data" in {
    when(mockS4LService.fetchAndGet[LeaseVehicles]()
      (Matchers.eq(S4LKey[LeaseVehicles]), any(), any()))
      .thenReturn(Future.successful(None))

    when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]()))
      .thenReturn(Future.successful(emptyVatScheme))

    callAuthorised(LeaseVehiclesController.show) {
      _ includesText "Is the company involved in leasing vehicles or equipment to customers?"
    }
  }

  s"POST ${routes.LeaseVehiclesController.show()} with Empty data" should {

    "return 400" in {
      AuthBuilder.submitWithAuthorisedUser(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.LeaseVehiclesController.submit()} with Lease Vehicles or Equipment - Yes selected" should {

    "redirects to next screen in the flow" in {
      val leaseVehicles = CacheMap("", Map("" -> Json.toJson(LeaseVehicles(true))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[LeaseVehicles](any())(any(), any(), any()))
        .thenReturn(Future.successful(leaseVehicles))

      AuthBuilder.submitWithAuthorisedUser(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "leaseVehiclesRadio" -> "true"
      ))(_ redirectsTo s"$contextRoot/company-bank-account")

    }
  }

  s"POST ${routes.LeaseVehiclesController.submit()} with Lease Vehicles or Equipment - No selected" should {

    "redirects to next screen in the flow" in {
      val leaseVehicles = CacheMap("", Map("" -> Json.toJson(LeaseVehicles(false))))

      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(Future.successful(()))

      when(mockS4LService.saveForm[LeaseVehicles](any())(any(), any(), any()))
        .thenReturn(Future.successful(leaseVehicles))

      AuthBuilder.submitWithAuthorisedUser(LeaseVehiclesController.submit(), fakeRequest.withFormUrlEncodedBody(
        "leaseVehiclesRadio" -> "false"
      ))(_ redirectsTo s"$contextRoot/provides-investment-fund-management-services")

    }
  }
}