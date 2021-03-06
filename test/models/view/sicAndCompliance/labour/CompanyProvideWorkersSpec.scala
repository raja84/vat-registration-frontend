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

package models.view.sicAndCompliance.labour

import fixtures.VatRegistrationFixture
import models.ApiModelTransformer
import models.api.VatComplianceLabour
import uk.gov.hmrc.play.test.UnitSpec

class CompanyProvideWorkersSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {

    "convert VatScheme without SicAndCompliance to empty view model" in {
      val vs = vatScheme(sicAndCompliance = None)
      ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme without LabourCompliance section to empty view model" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = None)))
      ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with LabourCompliance section to view model -  Company Does not Provide Workers " in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = Some(VatComplianceLabour(labour = false)))))
      ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs) shouldBe Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_NO))
    }

    "convert VatScheme with LabourCompliance section to view model - Company Does Provide Workers" in {
      val vs = vatScheme(sicAndCompliance = Some(vatSicAndCompliance(labourComplianceSection = Some(VatComplianceLabour(labour = true)))))
      ApiModelTransformer[CompanyProvideWorkers].toViewModel(vs) shouldBe Some(CompanyProvideWorkers(CompanyProvideWorkers.PROVIDE_WORKERS_YES))
    }

  }
}

