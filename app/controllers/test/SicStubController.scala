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

package controllers.test

import javax.inject.Inject

import connectors.{KeystoreConnector, VatRegistrationConnector}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.test.SicStubForm
import models.view.test.SicStub
import models.{ComplianceQuestions, NoComplianceQuestions}
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService}

import scala.concurrent.Future

class SicStubController @Inject()(s4LService: S4LService, vatRegistrationConnector: VatRegistrationConnector,
                                  ds: CommonPlayDependencies) extends VatRegistrationController(ds) with CommonService {

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request => {

    for {
      sicCodes <- s4LService.fetchAndGet[SicStub]()

      sicStub = SicStub(
        sicCodes.map(_.sicCode1.getOrElse("")),
        sicCodes.map(_.sicCode2.getOrElse("")),
        sicCodes.map(_.sicCode3.getOrElse("")),
        sicCodes.map(_.sicCode4.getOrElse(""))
      )
      form = SicStubForm.form.fill(sicStub)
    } yield Ok(views.html.pages.test.sic_stub(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    SicStubForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test.sic_stub(formWithErrors)))
      }, {
        data: SicStub => {
          s4LService.saveForm[SicStub](data) map { _ =>
            ComplianceQuestions(data.sicCodes) match {
              case NoComplianceQuestions =>
                Redirect(controllers.vatFinancials.vatBankAccount.routes.CompanyBankAccountController.show())
              case _ =>
                Redirect(controllers.sicAndCompliance.routes.ComplianceIntroductionController.show())
            }
          }
        }
      })
  })

}