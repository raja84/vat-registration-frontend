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

package controllers.userJourney

import javax.inject.Inject

import controllers.{CommonPlayDependencies, VatRegistrationController}
import enums.DownstreamOutcome
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.RegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SignInOutController @Inject()(vatRegistrationService: RegistrationService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  def postSignIn: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        assertVatRegistrationFootprint {
          Redirect(controllers.userJourney.routes.WelcomeController.start())
        }
  }

  private def assertVatRegistrationFootprint(f: => Result)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {
    vatRegistrationService.assertRegistrationFootprint() map {
      case DownstreamOutcome.Success => f
      case DownstreamOutcome.Failure => InternalServerError(views.html.pages.error.restart())
    }
  }

}
