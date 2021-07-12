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

import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.examplefrontend.common.UserClientProperties
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest

class AgentSpec extends AbstractTest {

  val agentModel: Agent = Agent(arn = "ANAGENT")
  val agentJs: JsValue = Json.parse(
    s"""{
      "${UserClientProperties.backendARN}": "${agentModel.arn}"
      }""".stripMargin
  )
  "Agent" can {
    "format to json" should {
      "succeed" in {
        Json.toJson(agentModel) shouldBe agentJs
      }
    }
    "format from json" should {
      "succeed" in {
        Json.fromJson[Agent](agentJs) shouldBe JsSuccess(agentModel)
      }
    }
  }
}
