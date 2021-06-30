package connectors

import helpers.WireMockHelper
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Client, User}

class DataConnectorIt extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with BeforeAndAfterAll{

	lazy val connector: DataConnector = app.injector.instanceOf[DataConnector]

	val testClient: Client = Client("testCrn", "testName", "testBusiness", "testContact", 12, "testPostcode", "testBusinessType")
	val testClientJson = Json.toJson(testClient)

	val testUser: User = User("testCrn", "testPass")

	override def beforeAll(): Unit = {
		super.beforeAll()
		startWiremock()
	}

	override def afterAll(): Unit = {
		stopWiremock()
		super.afterAll()
	}

	"DataConnector" can {

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

	}

}
