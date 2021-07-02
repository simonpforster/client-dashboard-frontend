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
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{CRN, Client, User}

class DataConnectorIt extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll{

	lazy val connector: DataConnector = app.injector.instanceOf[DataConnector]

	val testClient: Client = Client("testCrn", "testName", "testBusiness", "testContact", 12, "testPostcode", "testBusinessType")
	val testClientJson = Json.toJson(testClient)

	val testUser: User = User("testCrn", "testPass")

	lazy val crn = Json.toJson(crnTest)

	val crnTest: CRN =CRN("crnTest")



	override def beforeAll(): Unit = {
		super.beforeAll()
		startWiremock()
	}

	override def afterAll(): Unit = {
		stopWiremock()
		super.afterAll()
	}

	"DataConnector" can {

		"delete" should{
			"succesfully delete a client"in{
				stubDelete("/delete-client",status = 204,"")
				val result:Boolean = await(connector.deleteClient(crnTest))
				result should be (true)
			}
		}

		"login" should {
			"succesfully receive a client" in {
				stubPost("/login", 200, Json.stringify(testClientJson))

				val result = await(connector.login(testUser))

				result shouldBe Some(testClient)
			}

			"fail because of unauthorized" in {
				stubPost("/login", 401, Json.stringify(testClientJson))

				val result = await(connector.login(testUser))

				result shouldBe None
			}

			"fail because of bad request" in {
				stubPost("/login", 400,"")

				val result = await(connector.login(testUser))

				result shouldBe None
			}
		}

		"arn" should {
			"" in {

			}
		}

	}

}
