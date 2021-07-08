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
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, CRN, Client, User}

class DataConnectorIt extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll {

  lazy val connector: DataConnector = app.injector.instanceOf[DataConnector]

  val testClient: Client = Client(
    crn = "testCrn",
    name = "testName",
    businessName = "testBusiness",
    contactNumber = "testContact",
    propertyNumber = 12,
    postcode = "testPostcode",
    businessType = "testBusinessType")

  val testClientJson: JsValue = Json.toJson(testClient)

  val testUser: User = User(
    crn = "testCrn",
    password = "testPass")

  lazy val crn: JsValue = Json.toJson(crnTest)

  val crnTest: CRN = CRN(
    crn = "crnTest")

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }


  "DataConnector" can {
    "delete" should {
      "succesfully delete a client" in {
        stubDelete(
          url = "/delete-client",
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe true
      }

      "not found client to delete" in {
        stubDelete(
          url = "/delete-client",
          status = NOT_FOUND,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe false
      }

      "bad request no client deleted" in {
        stubDelete(
          url = "/delete-client",
          status = BAD_REQUEST,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe false
      }
    }

    "login" should {
      "succesfully receive a client" in {
        stubPost(
          url = "/login",
          status = 200,
          responseBody = Json.stringify(testClientJson))

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe Some(testClient)
      }

      "receive a bad client" in {
        stubPost(
          url = "/login",
          status = 200,
          responseBody = "{}")

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }

      "fail because of unauthorized" in {
        stubPost(
          url = "/login",
          status = 401,
          responseBody = Json.stringify(testClientJson))

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }

      "fail because of bad request" in {
        stubPost(
          url = "/login",
          status = 400,
          responseBody = "")

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }
    }

    "add arn" should {
      "succeed" in {
        stubPatch(
          url = "/add-agent",
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, Agent("testArn")))

        result shouldBe true
      }

      "fail because of not found" in {
        stubPatch(
          url = "/add-agent",
          status = 404,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, Agent("testArn")))

        result shouldBe false
      }

      "fail because of conflict" in {
        stubPatch(
          url = "/add-agent",
          status = 409,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, Agent("testArn")))

        result shouldBe false
      }
    }

    "remove arn" should {
      "succeed" in {
        stubPatch(
          url = "/remove-agent",
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, Agent("testArn")))

        result shouldBe true
      }

      "fail because of not found" in {
        stubPatch(
          url = "/remove-agent",
          status = 404,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, Agent("testArn")))

        result shouldBe false
      }

      "fail because of conflict" in {
        stubPatch(
          url = "/remove-agent",
          status = 409,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, Agent("testArn")))

        result shouldBe false
      }
    }
  }

}
