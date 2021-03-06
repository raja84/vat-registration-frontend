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

package connectors

import org.joda.time.LocalDate
import testHelpers.VatRegSpec
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

class WSBankHolidaysConnectorSpec extends VatRegSpec {

  val testConnector = new WSBankHolidaysConnector(mockWSHttp, fakeApplication.injector.instanceOf[ServicesConfig])

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "bankHolidays" must {

    "return set of bank holidays for a specified division" in {
      val testHolidaySet = Map(
        "division1" -> BankHolidaySet("division1", List(
          BankHoliday("one", new LocalDate(2017, 3, 22)))),
        "division2" -> BankHolidaySet("division2", List(
          BankHoliday("one", new LocalDate(2017, 3, 22)),
          BankHoliday("another", new LocalDate(2017, 3, 23))))
      )
      mockHttpGET[Map[String, BankHolidaySet]]("any-url", testHolidaySet)

      await(testConnector.bankHolidays("division1")) shouldBe BankHolidaySet("division1", List(
        BankHoliday("one", new LocalDate(2017, 3, 22))))
    }

  }

}
