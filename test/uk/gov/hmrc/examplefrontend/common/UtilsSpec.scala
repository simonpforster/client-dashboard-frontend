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

package uk.gov.hmrc.examplefrontend.common

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, SEE_OTHER}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.controllers.{HomePageController, NameUpdateController}
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html.UpdateNamePage

import scala.concurrent.{ExecutionContext, Future}

class UtilsSpec extends AbstractTest {

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  val nameUpdatePage: UpdateNamePage = app.injector.instanceOf[UpdateNamePage]
  val homePageController: HomePageController = app.injector.instanceOf[HomePageController]
  val mockDataConnector: DataConnector = mock[DataConnector]

  object utils extends Utils(mockDataConnector, error)

  object nameUpdateController extends NameUpdateController(
    mcc = mcc,
    nameUpdatePage = nameUpdatePage,
    error = error,
    dataConnector = mockDataConnector,
    utils = utils
  )

  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext
  implicit val fakeRequestHomepage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.clients)
  implicit val fakeRequestNameUpdate: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateClientName(testClient.crn))
  implicit val fakeRequestNameSubmit: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "POST",
    path = UrlKeys.updateClientName(testClient.crn))

  object testUtils extends Utils(mockDataConnector, error)

  "notloggedInCheck()" can {
    "redirect" should {
      "return SEE_OTHER if crn is in session" in {
        val result: Future[Result] = homePageController.homepage(fakeRequestHomepage.withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe SEE_OTHER
      }
    }
  }
  "loggedInCheckNoClient()" can {
    "redirect" should {
      "return SEE_OTHER if no crn is in session" in {
        val result: Future[Result] = nameUpdateController.updateName()(fakeRequestNameUpdate)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
  "loggedInCheckAsync()" can {
    "return BAD_REQUEST if client doesn't exist" in {
      when(mockDataConnector.readOne(any())) thenReturn Future(None)
      val result: Future[Result] = nameUpdateController.updateNameSubmit()(fakeRequestNameSubmit.withSession(SessionKeys.crn -> testClient.crn))
      status(result) shouldBe BAD_REQUEST
    }
    "return INTERNAL_SERVER_ERROR if error in the database" in {
      when(mockDataConnector.readOne(any())) thenReturn Future.failed(new RuntimeException)
      val result: Future[Result] = nameUpdateController.updateNameSubmit()(fakeRequestNameSubmit.withSession(SessionKeys.crn -> testClient.crn))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
    "redirect if no crn in session" in {
      val result: Future[Result] = nameUpdateController.updateNameSubmit()(fakeRequestNameSubmit)
      status(result) shouldBe SEE_OTHER
    }
  }
}
