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

import common.exceptions.DownstreamExceptions
import connectors.KeystoreConnector
import fixtures.KeystoreFixture
import testHelpers.VatRegSpec
import uk.gov.hmrc.play.http.HeaderCarrier

class CommonServiceSpec extends VatRegSpec with KeystoreFixture {

  trait Setup {
    val service = new CommonService with DownstreamExceptions {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  implicit val hc = HeaderCarrier()

  "Calling fetchRegistrationId" should {
    "throw a RegistrationNotFoundException when the Registration ID is not found in keystore" in new Setup {
      mockKeystoreFetchAndGet[String]("", None)
      a[DownstreamExceptions.RegistrationIdNotFoundException] shouldBe thrownBy(await(service.fetchRegistrationId))
    }
  }

}
