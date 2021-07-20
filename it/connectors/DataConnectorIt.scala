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
import play.api.http.Status.{BAD_REQUEST, CONFLICT, IM_A_TEAPOT, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK, UNAUTHORIZED}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.common.UrlKeys
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, Client, User}

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
    crn = testClient.crn,
    password = "testPass")

  val testNewName: String = "testNewName"

  val testARN: Agent = Agent("testArn")

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
          url = UrlKeys.deleteClient(testClient.crn),
          status = NO_CONTENT,
          responseBody = "")
        val result: Boolean = await(connector.deleteClient(testClient.crn))
        result shouldBe true
      }

      "not found client to delete" in {
        stubDelete(
          url = UrlKeys.deleteClient(testClient.crn),
          status = NOT_FOUND,
          responseBody = "")
        val result: Boolean = await(connector.deleteClient(testClient.crn))
        result shouldBe false
      }
      "bad request no client deleted" in {
        stubDelete(
          url = UrlKeys.deleteClient(testClient.crn),
          status = BAD_REQUEST,
          responseBody = "")
        val result: Boolean = await(connector.deleteClient(testClient.crn))
        result shouldBe false
      }
    }

    "readOne" should {
      "return Some client" in {
        stubGet(
          url = UrlKeys.readOneClient(testClient.crn),
          status = OK,
          responseBody = Json.stringify(testClientJson))
        val result: Option[Client] = await(connector.readOne(testClient.crn))
        result shouldBe Some(testClient)
      }

      "return None" in {
        stubGet(
          url = UrlKeys.readOneClient(testClient.crn),
          status = OK,
          responseBody = "{}")
        val result: Option[Client] = await(connector.readOne(testClient.crn))
        result shouldBe None
      }

      "throw an exception" in {
        stubGet(
          url = UrlKeys.readOneClient(testClient.crn),
          status = NOT_FOUND,
          responseBody = "{}")

        assertThrows[Exception] {
          val result: Option[Client] = await(connector.readOne(testClient.crn))
          result shouldBe new Exception("NotFound")
        }
      }
    }
  }

  "login" should {
    "succesfully receive a client" in {
      stubPost(
        url = UrlKeys.login(testClient.crn),
        status = OK,
        responseBody = Json.stringify(testClientJson))
      val result: Option[Client] = await(connector.login(testUser))
      result shouldBe Some(testClient)
    }

    "receive a bad client" in {
      stubPost(
        url = UrlKeys.login(testClient.crn),
        status = OK,
        responseBody = "{}")
      val result: Option[Client] = await(connector.login(testUser))
      result shouldBe None
    }

    "fail because of unauthorized" in {
      stubPost(
        url = UrlKeys.login(testClient.crn),
        status = UNAUTHORIZED,
        responseBody = Json.stringify(testClientJson))
      val result: Option[Client] = await(connector.login(testUser))
      result shouldBe None
    }

    "fail because of bad request" in {
      stubPost(
        url = UrlKeys.login(testClient.crn),
        status = BAD_REQUEST,
        responseBody = "")
      val result: Option[Client] = await(connector.login(testUser))
      result shouldBe None
    }
  }

  "add arn" should {
    "succeed" in {
      stubPatch(
        url = UrlKeys.addAgent(testClient.crn),
        status = NO_CONTENT,
        responseBody = "")
      val result: Boolean = await(connector.addArn(testClient.crn, "testArn"))
      result shouldBe true
    }

    "fail because of not found" in {
      stubPatch(
        url = UrlKeys.addAgent(testClient.crn),
        status = NOT_FOUND,
        responseBody = "")

      val result: Boolean = await(connector.addArn(testClient.crn, "testArn"))

      result shouldBe false
    }

    "fail because of conflict" in {
      stubPatch(
        url = UrlKeys.addAgent(testClient.crn),
        status = CONFLICT,
        responseBody = "")

      val result: Boolean = await(connector.addArn(testClient.crn, "testArn"))

      result shouldBe false
    }
  }

  "remove arn" should {
    "succeed" in {
      stubPatch(
        url = UrlKeys.removeAgent(testClient.crn),
        status = NO_CONTENT,
        responseBody = "")
      val result: Boolean = await(connector.removeArn(testClient.crn, testARN.arn))
      result shouldBe true
    }

    "fail because of not found" in {
      stubPatch(
        url = UrlKeys.removeAgent(testClient.crn),
        status = NOT_FOUND,
        responseBody = "")
      val result: Boolean = await(connector.removeArn(testClient.crn, testARN.arn))
      result shouldBe false
    }

    "fail because of conflict" in {
      stubPatch(
        url = UrlKeys.removeAgent(testClient.crn),
        status = CONFLICT,
        responseBody = "")
      val result: Boolean = await(connector.removeArn(testClient.crn, testARN.arn))
      result shouldBe false
    }
  }

  "updateContactNumber" can {
    "succeed" in {
      stubPatch(
        url = UrlKeys.updateContactNumber(testClient.crn),
        status = NO_CONTENT,
        responseBody = "")
      val result = await(connector.updateContactNumber(testClient.crn, testClient.contactNumber))
      result shouldBe true
    }

    "fail" in {
      stubPatch(
        url = UrlKeys.updateContactNumber(testClient.crn),
        status = INTERNAL_SERVER_ERROR,
        responseBody = "")
      val result = await(connector.updateContactNumber(testClient.crn, testClient.contactNumber))
      result shouldBe false
    }
  }


  "updateBusinessType" can {
    "succeed" in {
      stubPatch(
        url = UrlKeys.updateBusiness(testClient.crn),
        status = NO_CONTENT,
        responseBody = "")
      val result = await(connector.updateBusinessType(testClient.crn, testClient.businessType))
      result shouldBe true
    }

    "fail" in {
      stubPatch(
        url = UrlKeys.updateBusiness(testClient.crn),
        status = INTERNAL_SERVER_ERROR,
        responseBody = "")
      val result = await(connector.updateBusinessType(testClient.crn, testClient.businessType))
      result shouldBe false
    }
  }

  "updateClientName() PATCH REQUEST" should {
    "succeed" in {
      stubPatch(
        url = UrlKeys.updateClientName(testClient.crn),
        status = NO_CONTENT,
        responseBody = "")
      val result: Boolean = await(connector.updateClientName(testClient.crn, testNewName))
      result shouldBe true
    }

    "fail" in {
      stubPatch(
        url = UrlKeys.updateClientName(testClient.crn),
        status = INTERNAL_SERVER_ERROR,
        responseBody = "")
      val result = await(connector.updateClientName(testClient.crn, testNewName))
      result shouldBe false
    }
  }

  "updatePropertyDetails" should {
    "update property information" in {
      stubPatch(
        url = UrlKeys.updateProperty(testClient.crn),
        status = NO_CONTENT,
        responseBody = Json.stringify(testClientJson))
      val result: Boolean = await(connector.updateProperyDetails(testClient.propertyNumber, testClient.postcode, testClient.crn))
      result shouldBe true
    }

    "fail update property information" in {
      stubPatch(
        url = UrlKeys.updateProperty(testClient.crn),
        status = NOT_FOUND,
        responseBody = "{}")
      val result: Boolean = await(connector.updateProperyDetails(testClient.propertyNumber, testClient.postcode, testClient.crn))
      result shouldBe false
    }
  }

  "error handler" can {
    "fail because of teapot" in {
      stubPatch(
        url = UrlKeys.removeAgent(testClient.crn),
        status = IM_A_TEAPOT,
        responseBody = "")
      val result: Boolean = await(connector.removeArn(testClient.crn, testARN.arn))
      result shouldBe false
    }
  }
}



