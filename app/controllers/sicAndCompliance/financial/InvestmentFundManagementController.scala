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

package controllers.sicAndCompliance.financial

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.sicAndCompliance.financial.InvestmentFundManagementForm
import models.ElementPath
import models.view.sicAndCompliance.financial.InvestmentFundManagement
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import services.{RegistrationService, S4LService}

import scala.concurrent.Future


class InvestmentFundManagementController @Inject()(ds: CommonPlayDependencies)
                                                  (implicit s4LService: S4LService, vrs: RegistrationService) extends VatRegistrationController(ds) {
  import cats.instances.future._

  val form: Form[InvestmentFundManagement] = InvestmentFundManagementForm.form

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    viewModel[InvestmentFundManagement].fold(form)(form.fill)
      .map(f => Ok(views.html.pages.sicAndCompliance.financial.investment_fund_management(f)))
  )

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.sicAndCompliance.financial.investment_fund_management(formWithErrors)))
      }, {
        data: InvestmentFundManagement => {
          s4LService.saveForm[InvestmentFundManagement](data) flatMap { _ =>
            if (data.yesNo) {
              Future.successful(Redirect(controllers.sicAndCompliance.financial.routes.ManageAdditionalFundsController.show()))
            } else {
              vrs.deleteElements(ElementPath.finCompElementPaths.drop(5)).map { _ =>
                Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show()) }
            }
          }
        }
      }
    )
  )

}
