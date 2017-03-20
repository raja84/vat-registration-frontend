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
import models.api.{VatScheme, VatSicAndCompliance}
import models.view.sicAndCompliance.BusinessActivityDescription
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class BusinessActivityDescriptionSpec extends UnitSpec with VatRegistrationFixture {
  val description_1 = "testing-1"
  val description_2 = "testing-2"
  val businessActivityDescription_1 = BusinessActivityDescription(description_1)
  val businessActivityDescription_2 = BusinessActivityDescription(description_2)

  val sicAndCompliance = VatSicAndCompliance(description_1, None)
  val differentSicAndCompliance = VatSicAndCompliance(description_2, None)

  val vatScheme = VatScheme(id = validRegId, sicAndCompliance = Some(sicAndCompliance))

  "toApi" should {
    "update a SicAndCompliance with new BusinessActivityDescription" in {
      ViewModelTransformer[BusinessActivityDescription, VatSicAndCompliance]
        .toApi(businessActivityDescription_2, sicAndCompliance) shouldBe differentSicAndCompliance
    }
  }

  "apply" should {
    "Extract a BusinessActivityDescription view model from a VatScheme" in {
      ApiModelTransformer[BusinessActivityDescription].toViewModel(vatScheme) shouldBe Some(businessActivityDescription_1)
    }
    "Extract an empty BusinessActivityDescription view model from a VatScheme without sicAndCompliance" in {
      val vatSchemeWithoutSicAndCompliance = VatScheme(id = validRegId, sicAndCompliance = None)
      ApiModelTransformer[BusinessActivityDescription].toViewModel(vatSchemeWithoutSicAndCompliance) shouldBe None
    }
  }
}
