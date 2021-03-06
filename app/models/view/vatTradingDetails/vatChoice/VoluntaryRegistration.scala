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

import models.api.VatChoice.NECESSITY_VOLUNTARY
import models.api.{VatChoice, VatScheme, VatTradingDetails}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.Json

case class VoluntaryRegistration(yesNo: String)

object VoluntaryRegistration {

  val REGISTER_YES = "REGISTER_YES"
  val REGISTER_NO = "REGISTER_NO"

  val yes = VoluntaryRegistration(REGISTER_YES)
  val no = VoluntaryRegistration(REGISTER_NO)

  val valid = (item: String) => List(REGISTER_YES, REGISTER_NO).contains(item.toUpperCase)

  implicit val format = Json.format[VoluntaryRegistration]

  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.tradingDetails.map(_.vatChoice.necessity).collect {
      case NECESSITY_VOLUNTARY => VoluntaryRegistration(REGISTER_YES)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VoluntaryRegistration, g: VatTradingDetails) =>
    g.copy(vatChoice = g.vatChoice.copy(
      necessity = if (c.yesNo == REGISTER_YES) VatChoice.NECESSITY_VOLUNTARY else VatChoice.NECESSITY_OBLIGATORY
    ))
  }

}