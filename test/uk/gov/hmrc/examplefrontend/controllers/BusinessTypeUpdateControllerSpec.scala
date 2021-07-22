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
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, SEE_OTHER}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class BusinessTypeUpdateControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateBusinessTypePage: UpdateBusinessTypePage = app.injector.instanceOf[UpdateBusinessTypePage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext

  object testUpdateClientController extends BusinessTypeUpdateController(
    mcc,
    updateBusinessTypePage,
    mockDataConnector,
    error,
    executionContext
  )

  val fakeRequestBusinessType: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateBusiness
  )
  val fakeRequestSubmitBusinessType: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "POST",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.updateBusiness
  )

  "updateBusinessType GET " should {
    "return status Ok " when {
      "session/crn exists " in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(testClient))
        val result = testUpdateClientController.updateBusinessType().apply(fakeRequestBusinessType
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        contentAsString(result) should include("Update Business Type")
      }
    }

    "returns status See_Other " when {
      "no session/crn exists " in {
        val result: Future[Result] = testUpdateClientController.updateBusinessType().apply(fakeRequestBusinessType)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "submitBusinessTypeUpdate POST " should {
    "return status See_Other " when {
      "no session/crn exists " in {
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestSubmitBusinessType)
        status(result) shouldBe SEE_OTHER
      }
      "session/crn exists, form without errors and updateBusinessType connector returns true" in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestSubmitBusinessType
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe SEE_OTHER
      }
    }
    "return status NotImplemented" when {
      "session/crn exists, form without errors and updateBusinessType connector returns false " in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestSubmitBusinessType
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
    "return status InternalServerError" when {
      "session/crn exists, form without errors and updateBusinessType connector fails " in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestSubmitBusinessType
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
    "return status BadRequest " when {
      "session/crn exists and there are form with errors " in {
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestSubmitBusinessType
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.businessType -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
