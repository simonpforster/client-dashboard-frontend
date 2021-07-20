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

package connectors

import helpers.WireMockHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.common.UrlKeys
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Agent

class AgentConnectorit extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll {

  override val wiremockPort = 9009

  lazy val connector: DataConnector = app.injector.instanceOf[DataConnector]
  val testAgent: Agent = Agent(
    arn = "ARN766250F5")
  val badAgent: Agent = Agent(
    arn = "Fake"
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  "AgentConnector" can {
    "checkARN" should {
      "succesfully check arn" in {
        stubGet(
          url = UrlKeys.readAgent(testAgent.arn),
          status = OK,
          responseBody = "{}")
        val result: Boolean = await(connector.checkArn(testAgent))
        result shouldBe true
      }
      "agent not found" in {
        stubGet(
          url = UrlKeys.readAgent(testAgent.arn),
          status = NOT_FOUND,
          responseBody = "{}")
        val result: Boolean = await(connector.checkArn(badAgent))
        result shouldBe false
      }
      "bad request" in {
        stubGet(
          url = UrlKeys.readAgent(testAgent.arn),
          status = BAD_REQUEST,
          responseBody = "{}")
        val result: Boolean = await(connector.checkArn(testAgent))
        result shouldBe false
      }
    }
  }
}
