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

package models.view.test

import play.api.libs.json.Json

case class SicStub(sicCode1: Option[String],
                   sicCode2: Option[String],
                   sicCode3: Option[String],
                   sicCode4: Option[String]) {

  def sicCodes: List[String] = this.productIterator.toList.collect {
    case Some(s: String) if s.length == 8 => s.substring(0, 5)
  }

}

object SicStub {

  implicit val format = Json.format[SicStub]

}
