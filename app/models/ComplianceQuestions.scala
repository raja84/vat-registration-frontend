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

package models

import play.api.mvc.Call

sealed trait ComplianceQuestions {

  def firstQuestion: Call

}


case object CulturalComplianceQuestions extends ComplianceQuestions {

  override def firstQuestion: Call = controllers.userJourney.sicAndCompliance.routes.CulturalComplianceQ1Controller.show()

}

case object LabourComplianceQuestions extends ComplianceQuestions {

  //TODO change once controller merged
  override def firstQuestion: Call = controllers.userJourney.sicAndCompliance.routes.CulturalComplianceQ1Controller.show()

}

case object NoComplianceQuestions extends ComplianceQuestions {

  override def firstQuestion: Call = controllers.userJourney.vatFinancials.routes.CompanyBankAccountController.show()

}


object ComplianceQuestions {

  type SicCodeMap = Map[ComplianceQuestions, Vector[String]]

  implicit val sicCodeMaps: SicCodeMap = Map(
    CulturalComplianceQuestions -> Vector(
      "90010", "90020", "90030", "90040", "91012",
      "91020", "91030", "91040"),
    LabourComplianceQuestions -> Vector(
      "01610", "41201", "42110", "42910", "42990",
      "43120", "43999", "78200", "80100", "81210",
      "81221", "81222", "81223", "81291", "81299")
  )

  def apply(sicCodes: List[String])(implicit m: SicCodeMap): ComplianceQuestions = m.collectFirst {
    case (q, cs) if sicCodes.nonEmpty && sicCodes.forall(cs.contains) => q
  }.getOrElse(NoComplianceQuestions)

}
