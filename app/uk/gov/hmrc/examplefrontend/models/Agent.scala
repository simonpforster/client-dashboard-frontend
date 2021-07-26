/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.examplefrontend.models

import play.api.data.Forms.mapping
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, UserClientProperties}

case class Agent(arn: String)

object Agent {
  implicit val format: OFormat[Agent] = Json.format[Agent]
}

object AgentForm {
  val form: Form[Agent] =
    Form(
      mapping(
        UserClientProperties.arn -> Forms.text.verifying(ErrorMessages.agentFormError, _.nonEmpty)
      )(Agent.apply)(Agent.unapply)
    )
}
