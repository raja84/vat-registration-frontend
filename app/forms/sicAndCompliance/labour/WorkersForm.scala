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

package forms.sicAndCompliance.labour

import forms.FormValidation._
import forms.FormValidation.mandatoryNumericText
import models.view.sicAndCompliance.labour.Workers
import play.api.data.Form
import play.api.data.Forms._

object WorkersForm {
  val NUMBER_OF_WORKERS: String = "numberOfWorkers"

  implicit val errorCode: ErrorCode = "labourCompliance.numberOfWorkers"

  val form = Form(
    mapping(
      NUMBER_OF_WORKERS -> text.verifying(mandatoryNumericText).
        transform(numberOfWorkersToInt, intToText).verifying(boundedInt)
    )(Workers.apply)(Workers.unapply)
  )
}
