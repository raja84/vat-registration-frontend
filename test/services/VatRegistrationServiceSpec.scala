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

package services

import cats.data.OptionT
import cats.instances.future.catsStdInstancesForFuture
import cats.syntax.applicative.catsSyntaxApplicativeId
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.CompanyProvideWorkers
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.{S4LKey, VatBankAccountPath, ZeroRatedTurnoverEstimatePath}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()

  class Setup {

    val service = new VatRegistrationService(mockS4LService, mockRegConnector) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }

    def save4laterReturnsNothing[T: S4LKey]()(implicit s4LService: S4LService): Unit =
      when(s4LService.fetchAndGet[T]()(Matchers.eq(S4LKey[T]), any(), any())).thenReturn(None.pure)

    def save4laterReturns[T: S4LKey](t: T)(implicit s4lService: S4LService): Unit =
      when(s4lService.fetchAndGet[T]()(Matchers.eq(S4LKey[T]), any(), any())).thenReturn(OptionT.pure(t).value)

  }

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.createNewRegistration()(any(), any())).thenReturn(validVatScheme.pure)

      service.createRegistrationFootprint() completedSuccessfully
    }
  }

  "Calling submitVatScheme" should {
    "return a success response when the VatScheme is upserted" in new Setup {
      mockFetchRegId(validRegId)

      save4laterReturns(StartDateView(StartDateView.SPECIFIC_DATE, someTestDate))
      save4laterReturns(VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES))
      save4laterReturns(VoluntaryRegistrationReason(VoluntaryRegistrationReason.SELLS))
      save4laterReturns(TradingNameView(TradingNameView.TRADING_NAME_NO))
      save4laterReturns(validEstimateVatTurnover)
      save4laterReturns(validEstimateZeroRatedSales)
      save4laterReturns(validVatChargeExpectancy)
      save4laterReturns(validVatReturnFrequency)
      save4laterReturns(validAccountingPeriod)
      save4laterReturns(validBankAccountDetails)
      save4laterReturns(validBusinessActivityDescription)
      save4laterReturns(validNotForProfit)
      save4laterReturns(validCompanyProvideWorkers)
      save4laterReturns(validWorkers)
      save4laterReturns(validTemporaryContracts)
      save4laterReturns(validSkilledWorkers)
      save4laterReturns(validAdviceOrConsultancy)
      save4laterReturns(validActAsIntermediary)
      save4laterReturns(ChargeFees(true))
      save4laterReturns(LeaseVehicles(true))
      save4laterReturns(AdditionalNonSecuritiesWork(true))
      save4laterReturns(DiscretionaryInvestmentManagementServices(true))
      save4laterReturns(InvestmentFundManagement(true))
      save4laterReturns(ManageAdditionalFunds(true))
      save4laterReturns(validEuGoods)
      save4laterReturns(validApplyEori)
      save4laterReturns(validBusinessContactDetails)
      save4laterReturns(validServiceEligibility)

      when(mockRegConnector.upsertVatChoice(any(), any())(any(), any())).thenReturn(validVatChoice.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)
      when(mockRegConnector.upsertVatFinancials(any(), any())(any(), any())).thenReturn(validVatFinancials.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)
      when(mockRegConnector.upsertVatContact(any(), any())(any(), any())).thenReturn(validVatContact.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)

      service.submitVatScheme() completedSuccessfully
    }
  }

  "Calling submitTradingDetails" should {
    "return a success response when VatTradingDetails is submitted" in new Setup {
      mockFetchRegId(validRegId)
      save4laterReturns(TradingNameView(yesNo = TradingNameView.TRADING_NAME_YES))
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }

    "return a success response when start date choice is BUSINESS_START_DATE" in new Setup {

      val tradingDetailsWithCtActiveDateSelected = tradingDetails(startDateSelection = StartDateView.BUSINESS_START_DATE)

      mockFetchRegId(validRegId)
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(tradingDetailsWithCtActiveDateSelected.pure)
      save4laterReturns(StartDateView(StartDateView.BUSINESS_START_DATE, someTestDate))

      service.submitTradingDetails() returns tradingDetailsWithCtActiveDateSelected
    }

    "return a success response when VatTradingDetails is submitted and no Trading Name is found in S4L" in new Setup {
      mockFetchRegId(validRegId)
      save4laterReturnsNothing[TradingNameView]()
      save4laterReturns(validEuGoods)
      save4laterReturns(validApplyEori)
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatTradingDetails(any(), any())(any(), any())).thenReturn(validVatTradingDetails.pure)

      service.submitTradingDetails() returns validVatTradingDetails
    }
  }

  "Calling submitSicAndCompliance" should {
    "return a success response when SicAndCompliance is submitted" in new Setup {
      mockFetchRegId(validRegId)

      save4laterReturns(validBusinessActivityDescription)
      save4laterReturns(validNotForProfit)
      save4laterReturns(validCompanyProvideWorkers)
      save4laterReturns(validWorkers)
      save4laterReturns(validTemporaryContracts)
      save4laterReturns(validAdviceOrConsultancy)
      save4laterReturns(validActAsIntermediary)
      save4laterReturns(ChargeFees(true))
      save4laterReturns(AdditionalNonSecuritiesWork(true))
      save4laterReturns(DiscretionaryInvestmentManagementServices(true))
      save4laterReturns(InvestmentFundManagement(true))
      save4laterReturns(ManageAdditionalFunds(true))

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }

    "return a success response when SicAndCompliance is submitted and no Business Activity Description is found in S4L" in new Setup {
      mockFetchRegId(validRegId)

      save4laterReturnsNothing[BusinessActivityDescription]()
      save4laterReturnsNothing[CompanyProvideWorkers]()
      save4laterReturns(validSkilledWorkers)

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }
  }

  "Calling submitEligibility" should {
    "return a success response when VatEligibility is submitted" in new Setup {
      mockFetchRegId(validRegId)

      save4laterReturns(validServiceEligibility)

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)

      service.submitVatEligibility() returns validServiceEligibility
    }
  }

    "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(any())(any(), any())).thenReturn(().pure)

      service.deleteVatScheme() completedSuccessfully
    }
  }

  "Calling deleteElement" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElement(VatBankAccountPath) completedSuccessfully
    }
  }

  "Calling deleteElements with items" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElements(List(VatBankAccountPath, ZeroRatedTurnoverEstimatePath)) completedSuccessfully
    }
  }

  "Calling deleteElements without items" should {
    "return a success response when successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteElement(any())(any())(any(), any())).thenReturn(().pure)

      service.deleteElements(List()) completedSuccessfully
    }
  }

  "When this is the first time the user starts a journey and we're persisting to the backend" should {

    "submitVatFinancials should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatFinancials = VatFinancials(
        bankAccount = Some(validBankAccount),
        turnoverEstimate = 50000,
        zeroRatedTurnoverEstimate = Some(60000),
        reclaimVatOnMostReturns = true,
        accountingPeriods = monthlyAccountingPeriod
      )

      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)

      service.submitVatFinancials() returns mergedVatFinancials
    }

    "submitTradingDetails should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatTradingDetails = validVatTradingDetails
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)

      service.submitTradingDetails() returns mergedVatTradingDetails
    }


    "submitVatContact should process the submission even if VatScheme does not contain a VatContact object" in new Setup {
      val mergedvalidVatContact = validVatContact
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)

      service.submitVatContact() returns mergedvalidVatContact
    }

    "submitVatEligibility should process the submission even if VatScheme does not contain a VatEligibility object" in new Setup {
      val mergedVatServiceEligibility = validServiceEligibility
      when(mockRegConnector.getRegistration(Matchers.eq(validRegId))(any(), any())).thenReturn(emptyVatScheme.pure)

      service.submitVatEligibility() returns mergedVatServiceEligibility
    }


  }
}
