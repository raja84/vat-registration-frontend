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

import helpers.VATRegSpec
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class TaxableTurnoverControllerSpec extends VATRegSpec {

  val fakeRequest = FakeRequest("GET", "/taxable/turnover")

  "GET /taxable/turnover" must {
    "return 200" in {
      val result = new TaxableTurnoverController(ds).show(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = new TaxableTurnoverController(ds).show(fakeRequest)
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }
  }
}
