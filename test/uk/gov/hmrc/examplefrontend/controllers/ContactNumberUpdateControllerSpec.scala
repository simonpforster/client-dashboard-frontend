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
import play.api.http.Status
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, SEE_OTHER}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class ContactNumberUpdateControllerSpec extends AbstractTest{
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateContactNumber: UpdateContactNumber = app.injector.instanceOf[UpdateContactNumber]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext

  object testUpdateClientController extends ContactNumberUpdateController(
    mcc,
    updateContactNumber,
    mockDataConnector,
    error,
    executionContext
  )

  val fakeRequestContactNumber: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateContactNumber
  )
  val fakeRequestSubmitContactNumber: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "POST",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateContactNumber
  )

  "updateContactNumber() GET" should {
    "return status Ok" when {
      "session exists(user logged in)" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(testClient))
        val result = testUpdateClientController.updateContactNumber().apply(fakeRequestContactNumber
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        contentAsString(result) should include("Update Contact Number")
      }
    }

    "return status SeeOther" when {
      "session doesn't exists(user not logged in)" in {
        val result: Future[Result] = testUpdateClientController.updateContactNumber().apply(fakeRequestContactNumber)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "submitUpdatedContactNumber() POST" should {
    "return status BadRequest" when {
      "form with errors & with session(logged in)" in {
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequestSubmitContactNumber
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.contactNumber->""))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }

    "return status SeeOther" when {
      "form with errors & without session(not logged in)" in {
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestSubmitContactNumber)
        status(result) shouldBe SEE_OTHER
      }
    }

    "return Internal server error" when {
      "update contact number fails" in {
        when(mockDataConnector.updateContactNumber(any(),any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestSubmitContactNumber
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return status SeeOther" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns true" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestSubmitContactNumber
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe SEE_OTHER
      }
    }

    "return status NotImplemented" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns false" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestSubmitContactNumber
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
  }
}
