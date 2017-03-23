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

package forms.vatDetails.vatChoice

import java.time.LocalDate
import javax.inject.Inject

import common.Now
import forms.validation.FormValidation.Dates.{nonEmptyDateModel, validDateModel}
import forms.validation.FormValidation.inRange
import models.DateModel
import models.view.vatChoice.StartDate
import play.api.data.Form
import play.api.data.Forms._
import services.DateService
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

class StartDateFormFactory @Inject()(dateService: DateService, today: Now[LocalDate]) {

  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
  }

  val RADIO_INPUT_NAME = "startDateRadio"

  def form(): Form[StartDate] = {

    val minDate: LocalDate = dateService.addWorkingDays(today(), 2)
    val maxDate: LocalDate = today().plusMonths(3)

    Form(
      mapping(
        RADIO_INPUT_NAME -> nonEmptyText.verifying(StartDate.validSelection),
        "startDate" -> mandatoryIf(
          isEqual(RADIO_INPUT_NAME, StartDate.SPECIFIC_DATE),
          mapping(
            "day" -> text,
            "month" -> text,
            "year" -> text
          )(DateModel.apply)(DateModel.unapply)
            .verifying(
              nonEmptyDateModel("startDate"),
              validDateModel(inRange(minDate, maxDate, "startDate"), "startDate")
            ))
      )(StartDate.bind)(StartDate.unbind)
    )
  }

}