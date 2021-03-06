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

package controllers.sicAndCompliance.cultural

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.sicAndCompliance.cultural.NotForProfitForm
import models.FinancialCompliancePath
import models.view.sicAndCompliance.cultural.NotForProfit
import play.api.mvc.{Action, AnyContent}
import services.{S4LService, VatRegistrationService}


class NotForProfitController @Inject()(ds: CommonPlayDependencies)
                                      (implicit s4LService: S4LService, vrs: VatRegistrationService) extends VatRegistrationController(ds) {

  import cats.instances.future._
  import cats.syntax.applicative._

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    viewModel[NotForProfit].map { vm =>
      Ok(views.html.pages.sicAndCompliance.cultural.not_for_profit(NotForProfitForm.form.fill(vm)))
    }.getOrElse(Ok(views.html.pages.sicAndCompliance.cultural.not_for_profit(NotForProfitForm.form)))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    NotForProfitForm.form.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.pages.sicAndCompliance.cultural.not_for_profit(formWithErrors)).pure
      ,
      (data: NotForProfit) =>
        s4LService.saveForm[NotForProfit](data) flatMap { _ =>
          // TODO delete any existing non-cultural compliance questions
          vrs.deleteElement(FinancialCompliancePath).map { _ =>
            Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
          }
        }
    )
  })

}
