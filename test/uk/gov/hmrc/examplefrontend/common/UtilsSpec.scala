package uk.gov.hmrc.examplefrontend.common

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HttpEntity
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import play.api.mvc.{AnyContent, AnyContentAsEmpty, MessagesControllerComponents, MessagesRequest, Request, Result}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html.TestPage

import scala.concurrent.{ExecutionContext, Future}

class UtilsSpec extends AbstractTest {

	lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
	val error = app.injector.instanceOf[ErrorHandler]
	val testPage = app.injector.instanceOf[TestPage]
	val mockDataConnector: DataConnector = mock[DataConnector]



	implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext

	implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
		method = "GET",
		path = UrlKeys.dashboard)
		.withSession(SessionKeys.crn -> testClient.crn)


	object testUtils extends Utils(mockDataConnector, error)

	"loggedInCheck()" can {
		"succeed" should {
			"return a client" in {


				testUtils.loggedInCheckAsync(client => {Future(Ok(testPage())})(fakeRequest)
			}
		}
	}

}
