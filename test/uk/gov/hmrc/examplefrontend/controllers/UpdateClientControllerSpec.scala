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
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html.{UpdateClientPage, UpdateContactNumber}
import uk.gov.hmrc.examplefrontend.views.html.{DashboardPage, UpdateClientPage, UpdateClientPropertyPage}

import scala.concurrent.{ExecutionContext, Future}

class UpdateClientControllerSpec extends AbstractTest {


  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateClientPage: UpdateClientPage = app.injector.instanceOf[UpdateClientPage]
  lazy val updateContactNumber: UpdateContactNumber = app.injector.instanceOf[UpdateContactNumber]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  lazy val mockupdateClientPropertyPage: UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

  object testUpdateClientController extends UpdateClientController(
    mcc,
    updateClientPage,
    updateContactNumber,
    mockDataConnector,
    mockupdateClientPropertyPage,
    error,
    executionContext
  )

  private val client: Client = Client(
    crn = "CRN",
    name = "TestFullName",
    businessName = "TestNameOfBusiness",
    contactNumber = "01111111111111",
    propertyNumber = "10",
    postcode = "TestAddress",
    businessType = "Private Limited",
    arn = Some("Arn"))

  val fakeRequestProperty: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClientProperty
  ).withSession(SessionKeys.crn -> client.crn)

  val fakeRequestPropertyWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClientProperty
  )

  val fakeRequestClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)
    .withSession(SessionKeys.crn -> client.crn)

  val fakeRequestWithoutSessionClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)


  "OpenUpdateClientPage" should {
    "return status Ok" in {
      when(mockDataConnector.readOne(any()))
      .thenReturn(Future.successful(Some(client)))
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestClientPage)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include("Modify Account")
    }
    "return status BadRequest" in {
      when(mockDataConnector.readOne(any())) thenReturn Future.successful(None)
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestClientPage)
      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include("Something went wrong")
    }

    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestWithoutSessionClientPage)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "Update Client Property GET" should {
    "return status OK" in {
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientProperty(fakeRequestProperty)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
//      contentAsString(result) should include("Update Property")
    }
    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestWithoutSessionClientPage)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "UpdateClientPropertySubmit" should {
    "return status Redirect" when {
      "information when evrything passes" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(client))
        when(mockDataConnector.update(any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->client.propertyNumber,"postcode"->client.postcode))
        status(result) shouldBe Status.SEE_OTHER
      }
    }
    "return form with errors" when {
      "no information is present in form"in{
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->"","postcode"->""))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
    "return status redirect" when {
      "read one returns none" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(None)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->client.propertyNumber,"postcode"->client.postcode))
        status(result) shouldBe Status.NOT_FOUND
      }
    }
    "return status internal server error" when {
      "read one fails" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->client.propertyNumber,"postcode"->client.postcode))
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
    "return status Not Implemented" when {
      "update returns false"in{
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(client))
        when(mockDataConnector.update(any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->client.propertyNumber,"postcode"->client.postcode))
        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }
    "return status Not Found" when {
      "update fails"in{
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(client))
        when(mockDataConnector.update(any())) thenReturn  Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber"->client.propertyNumber,"postcode"->client.postcode))
        status(result) shouldBe Status.NOT_FOUND
      }
    }
    "return status redirect without crn" when {
      "no crn is present" in {
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestPropertyWithoutSession)
        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

  "updateContactNumber() GET" should {
    "return status Ok" when {
      "session exists(user logged in)" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(client))
        val result = testUpdateClientController.updateContactNumber(fakeRequest)

        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        contentAsString(result) should include("Update Contact Number")
      }
    }

    "return status SeeOther" when {
      "session doesn't exists(user not logged in)" in {
        val result = testUpdateClientController.updateContactNumber(fakeRequestWithoutSession)

        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

  "submitUpdatedContactNumber() POST" should {
    "return status BadRequest" when {
      "form with errors & with session(logged in)" in {
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }
    }

    "return status SeeOther" when {
      "form with errors & without session(not logged in)" in {
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequestWithoutSession)

        status(result) shouldBe Status.SEE_OTHER
      }
    }

    "return status SeeOther" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns true" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(true)
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequest.withFormUrlEncodedBody(UserClientProperties.contactNumber -> "01234567891"))

        status(result) shouldBe Status.SEE_OTHER
      }
    }

    "return status NotImplemented" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns false" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(false)
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequest.withFormUrlEncodedBody(UserClientProperties.contactNumber -> "01234567891"))

        status(result) shouldBe Status.NOT_IMPLEMENTED
      }
    }

    "return status InternalServerError" when {
      "form without errors & with session(logged in) & updateContactNumber connector fails" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.failed(new Exception)
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequest.withFormUrlEncodedBody(UserClientProperties.contactNumber -> "01234567891"))

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}
