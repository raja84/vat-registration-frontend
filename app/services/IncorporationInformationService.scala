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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import cats.data.OptionT
import com.google.inject.ImplementedBy
import connectors.{OptionalResponse, PPConnector}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

@ImplementedBy(classOf[IncorporationInformationService])
trait IIService {

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate]

}

class IncorporationInformationService @Inject()(ctConnector: PPConnector) extends IIService with CommonService {

  import cats.instances.future._

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCTActiveDate()(implicit headerCarrier: HeaderCarrier): OptionalResponse[LocalDate] =
    for {
      regId <- OptionT.liftF(fetchRegistrationId)
      ctReg <- ctConnector.getCompanyRegistrationDetails(regId)
      accountingDetails <- OptionT.fromOption(ctReg.accountingDetails)
      dateString <- OptionT.fromOption(accountingDetails.activeDate)
    } yield LocalDate.parse(dateString, formatter)

}