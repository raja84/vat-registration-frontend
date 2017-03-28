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

import javax.inject.Inject

import com.google.inject.ImplementedBy
import connectors.{KeystoreConnector, VatRegistrationConnector}
import enums.DownstreamOutcome
import enums.DownstreamOutcome._
import models.api._
import models.s4l.{S4LTradingDetails, S4LVatFinancials, S4LVatSicAndCompliance}
import models.view.sicAndCompliance.{BusinessActivityDescription, CulturalComplianceQ1}
import models.view.vatFinancials._
import models.view.vatTradingDetails.{StartDateView, TradingNameView, VoluntaryRegistration}
import models.{S4LKey, ViewModelTransformer}
import play.api.libs.json.Format
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[VatRegistrationService])
trait RegistrationService {

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value]

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme]

  def deleteBankAccountDetails()(implicit hc: HeaderCarrier): Future[Boolean]

  def deleteZeroRatedTurnover()(implicit hc: HeaderCarrier): Future[Boolean]

  def deleteAccountingPeriodStart()(implicit hc: HeaderCarrier): Future[Boolean]
}

class VatRegistrationService @Inject()(s4LService: S4LService, vatRegConnector: VatRegistrationConnector)
  extends RegistrationService
    with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  import cats.instances.future._
  import cats.syntax.cartesian._

  private def s4l[T: Format : S4LKey]()(implicit headerCarrier: HeaderCarrier) = s4LService.fetchAndGet[T]()

  private def update[C, G](c: Option[C], vs: VatScheme)(implicit vmTransformer: ViewModelTransformer[C, G]): G => G =
    g => c.map(vmTransformer.toApi(_, g)).getOrElse(g)

  def getVatScheme()(implicit hc: HeaderCarrier): Future[VatScheme] =
    fetchRegistrationId.flatMap(vatRegConnector.getRegistration)

  def deleteVatScheme()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteVatScheme)

  def deleteBankAccountDetails()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteBankAccount)

  def deleteZeroRatedTurnover()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteZeroRatedTurnover)

  def deleteAccountingPeriodStart()(implicit hc: HeaderCarrier): Future[Boolean] =
    fetchRegistrationId.flatMap(vatRegConnector.deleteAccountingPeriodStart)

  def assertRegistrationFootprint()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] =
    for {
      vatScheme <- vatRegConnector.createNewRegistration()
      _ <- keystoreConnector.cache[String]("RegistrationId", vatScheme.id)
    } yield Success

  def submitVatScheme()(implicit hc: HeaderCarrier): Future[DownstreamOutcome.Value] =
    submitTradingDetails |@| submitVatFinancials |@| submitSicAndCompliance map { case res@_ => Success }

  private def submitVatFinancials()(implicit hc: HeaderCarrier): Future[VatFinancials] = {

    // TODO: review this line (...(vs.financials.getOrElse(VatFinancials.empty))
    def mergeWithS4L(vs: VatScheme) =
      (s4l[EstimateVatTurnover]() |@|
        s4l[EstimateZeroRatedSales]() |@|
        s4l[VatChargeExpectancy]() |@|
        s4l[VatReturnFrequency]() |@|
        s4l[AccountingPeriod]() |@|
        s4l[CompanyBankAccountDetails]).map(S4LVatFinancials).map {
        s4l =>
          update(s4l.estimateVatTurnover, vs)
            .andThen(update(s4l.zeroRatedTurnoverEstimate, vs))
            .andThen(update(s4l.vatChargeExpectancy, vs))
            .andThen(update(s4l.vatReturnFrequency, vs))
            .andThen(update(s4l.accountingPeriod, vs))
            .andThen(update(s4l.companyBankAccountDetails, vs))
            .apply(vs.financials.getOrElse(VatFinancials.empty)) //TODO remove the "seeding" with default
      }

    for {
      vs <- getVatScheme()
      vatFinancials <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatFinancials(vs.id, vatFinancials)
    } yield response
  }

  private def submitSicAndCompliance()(implicit hc: HeaderCarrier): Future[VatSicAndCompliance] = {
    def mergeWithS4L(vs: VatScheme) =
      (s4l[BusinessActivityDescription]() |@|
        s4l[CulturalComplianceQ1]()).map(S4LVatSicAndCompliance).map {
        s4l =>
          update(s4l.description, vs)
            .andThen(update(s4l.culturalCompliance, vs))
            .apply(vs.vatSicAndCompliance.getOrElse(VatSicAndCompliance(""))) //TODO remove the "seeding" with default
      }

    for {
      vs <- getVatScheme()
      sicAndCompliance <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertSicAndCompliance(vs.id, sicAndCompliance)
    } yield response
  }

  private def submitTradingDetails()(implicit hc: HeaderCarrier): Future[VatTradingDetails] = {
    def mergeWithS4L(vs: VatScheme) =
      (s4l[TradingNameView]() |@|
        s4l[StartDateView]() |@|
        s4l[VoluntaryRegistration]()).map(S4LTradingDetails).map { s4l =>
        update(s4l.voluntaryRegistration, vs)
          .andThen(update(s4l.tradingName, vs))
          .andThen(update(s4l.startDate, vs))
          .apply(vs.tradingDetails.getOrElse(VatTradingDetails.empty)) //TODO remove the "seeding" with default
      }

    for {
      vs <- getVatScheme()
      vatTradingDetails <- mergeWithS4L(vs)
      response <- vatRegConnector.upsertVatTradingDetails(vs.id, vatTradingDetails)
    } yield response
  }

}
