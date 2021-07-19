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
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, NOT_IMPLEMENTED, OK, SEE_OTHER, SERVICE_UNAVAILABLE}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html._

import scala.concurrent.{ExecutionContext, Future}

class UpdateClientControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateClientPage: UpdateClientPage = app.injector.instanceOf[UpdateClientPage]
  lazy val updateContactNumber: UpdateContactNumber = app.injector.instanceOf[UpdateContactNumber]
  lazy val updateBusinessTypePage: UpdateBusinessTypePage = app.injector.instanceOf[UpdateBusinessTypePage]
  lazy val nameUpdatePage: UpdateNamePage = app.injector.instanceOf[UpdateNamePage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = Helpers.stubControllerComponents().executionContext
  lazy val updateClientPropertyPage: UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

  object testUpdateClientController extends UpdateClientController(
    mcc = mcc,
    updateClientPage = updateClientPage,
    nameUpdatePage = nameUpdatePage,
		updateClientPropertyPage = updateClientPropertyPage,
		updateContactNumberPage = updateContactNumber,
		updateBusinessTypePage= updateBusinessTypePage,
    error = error,
    dataConnector = mockDataConnector,
    ec = executionContext
  )

  val newName = "updatedClientName"
  val contentTypeMatch: String = "text/html"

  val fakeRequestProperty: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateProperty(testClient.crn)
  ).withSession(SessionKeys.crn -> testClient.crn)
  val fakeRequestPropertyWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateProperty(testClient.crn)
  )
  val fakeRequestContactNumber: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateContactNumber(testClient.crn)
  ).withSession(SessionKeys.crn -> testClient.crn)
  val fakeRequestContactNumberWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateContactNumber(testClient.crn)
  )
  val fakeRequestClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)
    .withSession(SessionKeys.crn -> testClient.crn)

  val fakeRequestBusinessType: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateBusiness(testClient.crn)
  ).withSession(SessionKeys.crn -> testClient.crn)
  val fakeRequestBusinessTypeWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateBusiness(testClient.crn)
  )
  val fakeRequestUpdate: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.updateClientName(testClient.crn))

  val fakeRequestWithoutSessionClientPage: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)
	val fakeRequestSubmitUpdate: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
		method = "POST",
		path = UrlKeys.updateClientName(testClient.crn))

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

  "Update Client Property GET" should {
    "return status OK" in {
      val result: Future[Result] = testUpdateClientController.openUpdateClientProperty(fakeRequestProperty)
      status(result) shouldBe OK
      contentType(result) shouldBe Some(contentTypeMatch)
    }
    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.openUpdateClientPage(fakeRequestWithoutSessionClientPage)
      status(result) shouldBe SEE_OTHER
    }
  }

  "UpdateClientPropertySubmit" should {
    "return status Redirect" when {
      "information when evrything passes" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe SEE_OTHER
      }
    }
    "return form with errors" when {
      "no information is present in form" in {
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> "", "postcode" -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }
    "return status Not Implemented" when {
      "update returns false" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
    "return status Not Found" when {
      "update fails" in {
        when(mockDataConnector.updateProperyDetails(any(), any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestProperty.withFormUrlEncodedBody("propertyNumber" -> testClient.propertyNumber, "postcode" -> testClient.postcode))
        status(result) shouldBe NOT_FOUND
      }
    }
    "return status redirect without crn" when {
      "no crn is present" in {
        val result: Future[Result] = testUpdateClientController.updateClientPropertySubmit(fakeRequestPropertyWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
    }
  }
  "updateContactNumber() GET" should {
    "return status Ok" when {
      "session exists(user logged in)" in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(testClient))
        val result = testUpdateClientController.updateContactNumber(fakeRequestContactNumber)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        contentAsString(result) should include("Update Contact Number")
      }
    }

    "return status SeeOther" when {
      "session doesn't exists(user not logged in)" in {
        val result: Future[Result] = testUpdateClientController.updateContactNumber(fakeRequestContactNumberWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "submitUpdatedContactNumber() POST" should {
    "return status BadRequest" when {
      "form with errors & with session(logged in)" in {
        val result = testUpdateClientController.submitUpdatedContactNumber(fakeRequestContactNumber.withFormUrlEncodedBody(UserClientProperties.contactNumber->""))
        status(result) shouldBe Status.BAD_REQUEST
      }
    }

    "return status SeeOther" when {
      "form with errors & without session(not logged in)" in {
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestContactNumberWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
    }

    "return Internal server error" when {
      "update contact number fails" in {
        when(mockDataConnector.updateContactNumber(any(),any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestContactNumber.withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return status SeeOther" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns true" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestContactNumber.withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe SEE_OTHER
      }
    }

    "return status NotImplemented" when {
      "form without errors & with session(logged in) & updateContactNumber connector returns false" in {
        when(mockDataConnector.updateContactNumber(any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.submitUpdatedContactNumber(fakeRequestContactNumber.withFormUrlEncodedBody(UserClientProperties.contactNumber -> testClient.contactNumber))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
  }

	"nameUpdate()" can {
		"successfully run" should {
			"return OK if crn in session" in {
				val result: Future[Result] = testUpdateClientController.updateName(fakeRequestUpdate.withSession(SessionKeys.crn -> testClient.crn))
				when(mockDataConnector.readOne(any())).thenReturn(Future.successful(Some(testClient)))
				status(result) shouldBe OK
			}

			"redirect login if no crn in session" in {
				val result: Future[Result] = testUpdateClientController.updateName(fakeRequestUpdate)
				status(result) shouldBe SEE_OTHER
			}
		}
 	}

	"submitNameUpdate()" can {
		"successfully pass info in the session" should {
			"return OK and passes updateName in session" in {
				when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(true))
				val result: Future[Result] = testUpdateClientController.updateNameSubmit(fakeRequestSubmitUpdate
					.withSession(SessionKeys.crn -> testClient.crn)
					.withFormUrlEncodedBody(UserClientProperties.name -> newName))
				status(result) shouldBe SEE_OTHER
			}
		}

		"fail with BAD REQUEST" should {
			"when no form is passed with" in {
				when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(true))
				val result: Future[Result] = testUpdateClientController.updateNameSubmit(fakeRequestSubmitUpdate
					.withSession(SessionKeys.crn -> testClient.crn)
					.withFormUrlEncodedBody(UserClientProperties.name -> ""))
				status(result) shouldBe BAD_REQUEST
			}
		}

		"fail with SERVICE UNAVAILABLE" should {
			"when update fails for unknown reason" in {
				when(mockDataConnector.updateClientName(any(), any())).thenReturn(Future(false))
				val result: Future[Result] = testUpdateClientController.updateNameSubmit(fakeRequestSubmitUpdate
					.withSession(SessionKeys.crn -> testClient.crn)
					.withFormUrlEncodedBody(UserClientProperties.name -> newName))
				status(result) shouldBe SERVICE_UNAVAILABLE
			}
		}

		"fail with BAD GATEWAY" should {
			"when accessed with no crn" in {
				val result: Future[Result] = testUpdateClientController.updateNameSubmit(fakeRequestSubmitUpdate
				  .withFormUrlEncodedBody(UserClientProperties.name -> newName))
				status(result) shouldBe SEE_OTHER
			}
		}

		"fail with NOT FOUND" should {
			"when RunTimeException thrown" in {
				when(mockDataConnector.updateClientName(any(), any())) thenReturn Future.failed(new RuntimeException)
				val result: Future[Result] = testUpdateClientController.updateNameSubmit(fakeRequestProperty
					.withFormUrlEncodedBody(UserClientProperties.name -> newName))
				status(result) shouldBe NOT_FOUND
			}
		}
	}

  "updateBusinessType GET " should {
    "return status Ok " when {
      "session/crn exists " in {
        when(mockDataConnector.readOne(any())) thenReturn Future.successful(Some(testClient))
        val result = testUpdateClientController.updateBusinessType(fakeRequestBusinessType)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        contentAsString(result) should include("Update Business Type")
      }
    }

    "returns status See_Other " when {
      "no session/crn exists " in {
        val result: Future[Result] = testUpdateClientController.updateBusinessType(fakeRequestBusinessTypeWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "submitBusinessTypeUpdate POST " should {
    "return status See_Other " when {
      "no session/crn exists " in {
        val result: Future[Result] = testUpdateClientController.updateBusinessType(fakeRequestBusinessTypeWithoutSession)
        status(result) shouldBe SEE_OTHER
      }
      "session/crn exists, form without errors and updateBusinessType connector returns true" in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.successful(true)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestBusinessType.withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe SEE_OTHER
      }
    }
    "return status NotImplemented" when {
      "session/crn exists, form without errors and updateBusinessType connector returns false " in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.successful(false)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestBusinessType.withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe NOT_IMPLEMENTED
      }
    }
    "return status InternalServerError" when {
      "session/crn exists, form without errors and updateBusinessType connector fails " in {
        when(mockDataConnector.updateBusinessType(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestBusinessType.withFormUrlEncodedBody(UserClientProperties.businessType -> testClient.businessType))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
    "return status BadRequest " when {
      "session/crn exists and there are form with errors " in {
        val result: Future[Result] = testUpdateClientController.submitBusinessTypeUpdate(fakeRequestBusinessType.withFormUrlEncodedBody(UserClientProperties.businessType -> ""))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
