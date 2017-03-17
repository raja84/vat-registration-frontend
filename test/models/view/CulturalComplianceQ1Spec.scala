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

package models.view

import fixtures.VatRegistrationFixture
import models.ApiModelTransformer
import models.api.VatScheme
import models.view.vatFinancials.CompanyBankAccount
import uk.gov.hmrc.play.test.UnitSpec

class CulturalComplianceQ1Spec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    val vatScheme = VatScheme(validRegId)

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme.copy(sicAndCompliance = None)
      ApiModelTransformer[CompanyBankAccount].toViewModel(vs) shouldBe None
    }
  }
}
