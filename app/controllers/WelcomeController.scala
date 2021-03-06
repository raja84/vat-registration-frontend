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

package controllers

import javax.inject.Inject

import play.api.mvc._
import services.RegistrationService

class WelcomeController @Inject()(vatRegistrationService: RegistrationService, ds: CommonPlayDependencies) extends VatRegistrationController(ds) {

  //access to root of application should by default direct the user to the proper URL for start of VAT registration
  def show: Action[AnyContent] = Action(implicit request => Redirect(routes.WelcomeController.start()))

  def start: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    vatRegistrationService.createRegistrationFootprint().map(_ => Ok(views.html.pages.welcome()))
  })

}
