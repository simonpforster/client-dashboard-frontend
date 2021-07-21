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

package uk.gov.hmrc.examplefrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class UpdateClientControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateClientPage: UpdateClientPage = app.injector.instanceOf[UpdateClientPage]
  lazy val updateBusinessTypePage: UpdateBusinessTypePage = app.injector.instanceOf[UpdateBusinessTypePage]
  lazy val nameUpdatePage: UpdateNamePage = app.injector.instanceOf[UpdateNamePage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext
  lazy val updateClientPropertyPage: UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

  object testUpdateClientController extends UpdateClientController(
    mcc = mcc,
    updateClientPage = updateClientPage,
    error = error,
    dataConnector = mockDataConnector,
    ec = executionContext
  )

  val contentTypeMatch: String = "text/html"

  val fakeRequestClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)
    .withSession(SessionKeys.crn -> testClient.crn)
  val fakeRequestWithoutSessionClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)


  "OpenUpdateClientPage" should {
    "return status Ok" in {
      when(mockDataConnector.readOne(any()))
        .thenReturn(Future.successful(Some(testClient)))
      val result: Future[Result] = testUpdateClientController.openUpdateClientPage(fakeRequestClientPage)
      status(result) shouldBe OK
      contentType(result) shouldBe Some(contentTypeMatch)
      contentAsString(result) should include("Modify Account")
    }

    "return status Internal Server error" in {
      when(mockDataConnector.readOne(any()))
        .thenReturn(Future.failed(new RuntimeException))
      val result: Future[Result] = testUpdateClientController.openUpdateClientPage(fakeRequestClientPage)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return status BadRequest" in {
      when(mockDataConnector.readOne(any())) thenReturn Future.successful(None)
      val result: Future[Result] = testUpdateClientController.openUpdateClientPage(fakeRequestClientPage)
      status(result) shouldBe BAD_REQUEST
      contentType(result) shouldBe Some(contentTypeMatch)
      contentAsString(result) should include("Something went wrong")
    }

    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.openUpdateClientPage(fakeRequestWithoutSessionClientPage)
      status(result) shouldBe SEE_OTHER
    }
  }
}
