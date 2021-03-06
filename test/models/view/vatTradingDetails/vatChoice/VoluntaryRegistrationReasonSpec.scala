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

package models.view.vatTradingDetails.vatChoice

import fixtures.VatRegistrationFixture
import models.api.VatTradingDetails
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistrationReason.{INTENDS_TO_SELL, NEITHER, SELLS}
import models.{ApiModelTransformer, ViewModelTransformer}
import org.scalatest.{Inspectors, Matchers}
import uk.gov.hmrc.play.test.UnitSpec

class VoluntaryRegistrationReasonSpec extends UnitSpec with Matchers with Inspectors with VatRegistrationFixture {

  private val validationFunction = VoluntaryRegistrationReason.valid

  "ViewModelTransformer" should {

    "update VatTradingDetails with new VoluntaryRegistrationReason (sells taxable goods)" in {
      val transformed = ViewModelTransformer[VoluntaryRegistrationReason, VatTradingDetails]
        .toApi(VoluntaryRegistrationReason.sells, tradingDetails(reason = Some(INTENDS_TO_SELL)))
      transformed.vatChoice.reason shouldBe Some(SELLS)
    }

    "update VatTradingDetails with new VoluntaryRegistrationReason (intends to sell taxable goods)" in {
      val transformed = ViewModelTransformer[VoluntaryRegistrationReason, VatTradingDetails]
        .toApi(VoluntaryRegistrationReason.intendsToSell, tradingDetails(reason = Some(SELLS)))
      transformed.vatChoice.reason shouldBe Some(INTENDS_TO_SELL)
    }

  }

  "ApiModelTransformer" should {

    "produce empty view model from an empty voluntary registration reason" in {
      val vm = ApiModelTransformer[VoluntaryRegistrationReason]
        .toViewModel(vatScheme(vatTradingDetails = Some(tradingDetails())))
      vm shouldBe None
    }

    "produce a view model from a vatScheme with voluntary registration reason (sells)" in {
      val vm = ApiModelTransformer[VoluntaryRegistrationReason]
        .toViewModel(vatScheme(vatTradingDetails = Some(tradingDetails(reason = Some(SELLS)))))
      vm shouldBe Some(VoluntaryRegistrationReason.sells)
    }

    "produce a view model from a vatScheme with voluntary registration reason (intends to sell)" in {
      val vm = ApiModelTransformer[VoluntaryRegistrationReason]
        .toViewModel(vatScheme(vatTradingDetails = Some(tradingDetails(reason = Some(INTENDS_TO_SELL)))))
      vm shouldBe Some(VoluntaryRegistrationReason.intendsToSell)
    }

  }

  "VoluntaryRegistrationReason is valid" when {

    "selected reason is one of the allowed values" in {
      forAll(Seq(SELLS, INTENDS_TO_SELL, NEITHER)) {
        validationFunction(_) shouldBe true
      }
    }

  }

  "VoluntaryRegistrationReason is NOT valid" when {

    "selected reason is not of the allowed values" in {
      forAll(Seq("", "not an allowed value")) {
        validationFunction(_) shouldBe false
      }
    }

  }

}
