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
import play.api.http.Status._
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys, UserClientProperties, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class NameUpdateControllerSpec extends AbstractTest {

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val nameUpdatePage: UpdateNamePage = app.injector.instanceOf[UpdateNamePage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  val utils: Utils = new Utils(dataConnector = mockDataConnector, error = error)
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext
  lazy val updateClientPropertyPage: UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

object testNameUpdateController extends NameUpdateController(
  mcc = mcc,
  nameUpdatePage = nameUpdatePage,
  error = error,
  dataConnector = mockDataConnector,
  utils = utils
)

  val newName = "updatedClientName"
  val fakeRequestClientName: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateClientName
  )
  val fakeRequestSubmitClientName: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "POST",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateClientName
  )

  "nameUpdate()" can {
    "successfully run" should {
      "return OK if crn in session" in {
        val result: Future[Result] = testNameUpdateController.updateName().apply(fakeRequestClientName
          .withSession(SessionKeys.crn -> testClient.crn))
        when(mockDataConnector.readOne(any())).thenReturn(Future.successful(Some(testClient)))
        status(result) shouldBe OK
      }

      "redirect login if no crn in session" in {
        val result: Future[Result] = testNameUpdateController.updateName().apply(fakeRequestClientName)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "submitNameUpdate()" can {
    "successfully pass info in the session" should {
      "return OK and passes updateName in session" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(true))
        val result: Future[Result] = testNameUpdateController.updateNameSubmit().apply(fakeRequestSubmitClientName
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.name -> newName))
        status(result) shouldBe SEE_OTHER
      }
    }

    "fail with BAD REQUEST" should {
      "when no form is passed with" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(true))
        val result: Future[Result] = testNameUpdateController.updateNameSubmit().apply(fakeRequestSubmitClientName
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.name -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "fail with SERVICE UNAVAILABLE" should {
      "when update fails for unknown reason" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(false))
        val result: Future[Result] = testNameUpdateController.updateNameSubmit().apply(fakeRequestSubmitClientName
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.name -> newName))
        status(result) shouldBe SERVICE_UNAVAILABLE
      }
    }

    "fail with BAD GATEWAY" should {
      "when accessed with no crn" in {
        val result: Future[Result] = testNameUpdateController.updateNameSubmit().apply(fakeRequestSubmitClientName
          .withFormUrlEncodedBody(UserClientProperties.name -> newName))
        status(result) shouldBe SEE_OTHER
      }
    }

    "fail with NOT FOUND" should {
      "when RunTimeException thrown" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.updateClientName(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testNameUpdateController.updateNameSubmit().apply(fakeRequestSubmitClientName
          .withSession(UserClientProperties.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.name -> newName))
        status(result) shouldBe NOT_FOUND
      }
    }
  }

}
