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
import org.scalatest.{BeforeAndAfterAll, stats}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.common.UrlKeys
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, CRN, Client, User}

class DataConnectorIt extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll {

  lazy val connector: DataConnector = app.injector.instanceOf[DataConnector]

  val testClient: Client = Client(
    crn = "testCrn",
    name = "testName",
    businessName = "testBusiness",
    contactNumber = "testContact",
    propertyNumber = "12",
    postcode = "testPostcode",
    businessType = "testBusinessType")

  val testClientJson: JsValue = Json.toJson(testClient)

  val testUser: User = User(
    crn = "testCrn",
    password = "testPass")

  lazy val crn: JsValue = Json.toJson(crnTest)
  val testARN: Agent = Agent("testArn")
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
          url = UrlKeys.deleteClient,
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe true
      }

      "not found client to delete" in {
        stubDelete(
          url = UrlKeys.deleteClient,
          status = NOT_FOUND,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe false
      }

      "read One Client" in {
        stubGet(
          url = UrlKeys.readOneClient,
          status = 200,
          responseBody = Json.stringify(testClientJson))

        val result: Option[Client] = await(connector.readOne(testClient.crn))
        result shouldBe Some(testClient)
      }

      "update One client" in {
        stubPut(
          url = UrlKeys.updateClient,
          status = 201,
          responseBody = Json.stringify(testClientJson))

        val result: Boolean = await(connector.update(testClient))
        result shouldBe true
      }

      "update property information" in{
        stubPatch(
          url = UrlKeys.updateProperty,
          status = 204,
          responseBody = Json.stringify(testClientJson))

        val result: Boolean = await(connector.updateProperyDetails(testClient.propertyNumber,testClient.postcode,testClient.crn))
        result shouldBe true
      }

      "bad request no client deleted" in {
        stubDelete(
          url = UrlKeys.deleteClient,
          status = BAD_REQUEST,
          responseBody = "")

        val result: Boolean = await(connector.deleteClient(crnTest))

        result shouldBe false
      }
    }

    "login" should {
      "succesfully receive a client" in {
        stubPost(
          url = UrlKeys.login,
          status = 200,
          responseBody = Json.stringify(testClientJson))

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe Some(testClient)
      }

      "receive a bad client" in {
        stubPost(
          url = UrlKeys.login,
          status = 200,
          responseBody = "{}")

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }

      "fail because of unauthorized" in {
        stubPost(
          url = UrlKeys.login,
          status = 401,
          responseBody = Json.stringify(testClientJson))

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }

      "fail because of bad request" in {
        stubPost(
          url = UrlKeys.login,
          status = 400,
          responseBody = "")

        val result: Option[Client] = await(connector.login(testUser))

        result shouldBe None
      }
    }

    "add arn" should {
      "succeed" in {
        stubPatch(
          url = UrlKeys.addAgent,
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, testARN))

        result shouldBe true
      }

      "fail because of not found" in {
        stubPatch(
          url = UrlKeys.addAgent,
          status = 404,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, testARN))

        result shouldBe false
      }

      "fail because of conflict" in {
        stubPatch(
          url = UrlKeys.addAgent,
          status = 409,
          responseBody = "")

        val result: Boolean = await(connector.addArn(testClient, testARN))

        result shouldBe false
      }
    }

    "remove arn" should {
      "succeed" in {
        stubPatch(
          url = UrlKeys.removeAgent,
          status = 204,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, testARN))

        result shouldBe true
      }

      "fail because of not found" in {
        stubPatch(
          url = UrlKeys.removeAgent,
          status = 404,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, testARN))

        result shouldBe false
      }

      "fail because of conflict" in {
        stubPatch(
          url = UrlKeys.removeAgent,
          status = 409,
          responseBody = "")

        val result: Boolean = await(connector.removeArn(testClient, testARN))

        result shouldBe false
      }
    }

    "updateContactNumber" can {
      "succeed" in {
        stubPatch(
          url = UrlKeys.updateContactNumber,
          status = 204,
          responseBody = "")

        val result = await(connector.updateContactNumber(testClient.crn, testClient.contactNumber))

        result shouldBe true
      }

      "fail" in {
        stubPatch(
          url = UrlKeys.updateContactNumber,
          status = 500,
          responseBody = "")

        val result = await(connector.updateContactNumber(testClient.crn, testClient.contactNumber))

        result shouldBe false
      }
    }

    "updateBusinessType" can {
      "succeed" in {
        stubPatch(
          url = UrlKeys.updateBusiness,
          status = 204,
          responseBody = "")

        val result = await(connector.updateBusinessType(testClient.crn, testClient.businessType))

        result shouldBe true
      }

      "fail" in {
        stubPatch(
          url = UrlKeys.updateBusiness,
          status = 500,
          responseBody = "")

        val result = await(connector.updateBusinessType(testClient.crn, testClient.businessType))

        result shouldBe false
      }
    }
  }

  "error handler" can {
    "fail because of teapot" in {
      stubPatch(
        url = UrlKeys.removeAgent,
        status = 418,
        responseBody = "")

      val result: Boolean = await(connector.removeArn(testClient, testARN))

      result shouldBe false
    }
  }

}
