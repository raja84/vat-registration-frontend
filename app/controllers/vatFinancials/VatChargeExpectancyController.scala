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

package controllers.vatFinancials

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatFinancials.VatChargeExpectancyForm
import models.view.vatFinancials.VatChargeExpectancy
import models.view.vatFinancials.vatAccountingPeriod.VatReturnFrequency
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}

import scala.concurrent.Future


class VatChargeExpectancyController @Inject()(ds: CommonPlayDependencies)
                                             (implicit s4LService: S4LService,
                                              vatRegistrationService: VatRegistrationService)
  extends VatRegistrationController(ds) {
  import cats.instances.future._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[VatChargeExpectancy].map { vm =>
      Ok(views.html.pages.vatFinancials.vat_charge_expectancy(VatChargeExpectancyForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.vatFinancials.vat_charge_expectancy(VatChargeExpectancyForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    VatChargeExpectancyForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.vatFinancials.vat_charge_expectancy(formWithErrors)))
      }, {
        data: VatChargeExpectancy => {
          s4LService.saveForm[VatChargeExpectancy](data) flatMap { _ =>
            if (VatChargeExpectancy.VAT_CHARGE_NO == data.yesNo) {
              s4LService.saveForm[VatReturnFrequency](VatReturnFrequency(VatReturnFrequency.QUARTERLY))
                .map { _ => Redirect(controllers.vatFinancials.vatAccountingPeriod.routes.AccountingPeriodController.show()) }
            } else {
              Future.successful(Redirect(controllers.vatFinancials.vatAccountingPeriod.routes.VatReturnFrequencyController.show()))
            }
          }
        }
      })
  })

}
