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
import play.api.test.Helpers.{contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class PropertyUpdateControllerSpec  extends AbstractTest{

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext
  lazy val updateClientPropertyPage: UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

  object testUpdateClientController extends PropertyUpdateController(
    mcc = mcc,
    updateClientPropertyPage = updateClientPropertyPage,
    error = error,
    dataConnector = mockDataConnector,
    ec = executionContext
  )

  val contentTypeMatch: String = "text/html"

  val fakeRequestProperty: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateProperty(testClient.crn)
  ).withSession(SessionKeys.crn -> testClient.crn)
  val fakeRequestPropertyWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateProperty(testClient.crn)
  )

  "Update Client Property GET" should {
    "return status OK" in {
      val result: Future[Result] = testUpdateClientController.openUpdateClientProperty(fakeRequestProperty)
      status(result) shouldBe OK
      contentType(result) shouldBe Some(contentTypeMatch)
    }
    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.openUpdateClientProperty(fakeRequestPropertyWithoutSession)
      status(result) shouldBe SEE_OTHER
    }
  }

  "UpdateClientPropertySubmit" should {
    "return status Redirect" when {
      "information when evrything passes" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit()(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe SEE_OTHER
      }
    }
    "return form with errors" when {
      "no information is present in form" in {
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit()(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> "", "postcode" -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }
    "return status Not Implemented" when {
      "update returns false" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit()(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
    "return status Not Found" when {
      "update fails" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit()(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe NOT_FOUND
      }
    }
    "return status redirect without crn" when {
      "no crn is present" in {
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit()(fakeRequestPropertyWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
