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

import models.api.VatScheme

trait ViewModelTransformer[C, G] {
  // Upserts (selectively converts) a View model object to its API model counterpart
  def toApi(component: C, group: G): G
}

object ViewModelTransformer {
  def apply[C, G](implicit vmTransformer: ViewModelTransformer[C, G]): ViewModelTransformer[C, G] = vmTransformer

  def apply[C, G](f: (C, G) => G): ViewModelTransformer[C, G] = new ViewModelTransformer[C, G] {
    override def toApi(component: C, group: G): G = f(component, group)
  }
}

trait ApiModelTransformer[VM] {
  // Returns a view model for a specific part of a given VatScheme API model
  def toViewModel(vatScheme: VatScheme): Option[VM]
}

object ApiModelTransformer {
  def apply[T: ApiModelTransformer]: ApiModelTransformer[T] = implicitly

  def apply[T](f: VatScheme => Option[T]): ApiModelTransformer[T] = new ApiModelTransformer[T] {
    override def toViewModel(vatScheme: VatScheme): Option[T] = f(vatScheme)
  }
}