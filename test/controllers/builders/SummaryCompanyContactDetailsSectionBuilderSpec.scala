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

package controllers.builders

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatContact, VatDigitalContact}
import models.view.SummaryRow

class SummaryCompanyContactDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing the company contact details section" should {

    val vatContact = VatContact(
      digitalContact = VatDigitalContact(
        email = "some@email.com",
        tel = Some("0123456789"),
        mobile = Some("0123456789")
      ),
      website = Some("http://website.com")
    )

    val sectionBuilder = SummaryCompanyContactDetailsSectionBuilder(Some(vatContact))


    "render Business Email row" in {
      sectionBuilder.businessEmailRow mustBe SummaryRow(
        id = "companyContactDetails.email",
        answerMessageKey = "some@email.com",
        changeLink = Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
      )
    }

    "render Business Daytime Phone Number row" in {
      sectionBuilder.businessDaytimePhoneNumberRow mustBe SummaryRow(
        id = "companyContactDetails.daytimePhone",
        answerMessageKey = "0123456789",
        changeLink = Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
      )
    }

    "render Business Mobile Phone Number row" in {
      sectionBuilder.businessMobilePhoneNumberRow mustBe SummaryRow(
        id = "companyContactDetails.mobile",
        answerMessageKey = "0123456789",
        changeLink = Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
      )
    }

    "render Business Website row" in {
      sectionBuilder.businessWebsiteRow mustBe SummaryRow(
        id = "companyContactDetails.website",
        answerMessageKey = "http://website.com",
        changeLink = Some(controllers.vatContact.routes.BusinessContactDetailsController.show())
      )
    }

    "summary section shows and hides rows" in {
      val testCases = Seq(
        //pair of VatContact and expectedNumberOfRows to be produced in the summary
        (VatContact(VatDigitalContact(email = "email")), 1),
        (VatContact(VatDigitalContact(email = "email", tel = Some("123"))), 2),
        (VatContact(VatDigitalContact(email = "email", mobile = Some("123"))), 2),
        (VatContact(VatDigitalContact(email = "email", tel = Some("123"), mobile = Some("123"))), 3),
        (vatContact, 4)
      )
      forAll(testCases) { test =>
        val (contact, expectedNumberOfRows) = test
        val section = SummaryCompanyContactDetailsSectionBuilder(Some(contact)).section
        section.id mustBe "companyContactDetails"
        section.rows.count(_._2) mustEqual expectedNumberOfRows
      }
    }
  }
}