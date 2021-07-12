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


import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object testDashboardController extends DashboardController(
    mcc = mcc,
    dashboardPage = dashboardPage,
    dataConnector = mockDataConnector,
    error = error,
    ec = executionContext
  )

  val agentFormField: String = "arn"
  val testCRN: String = "testCrn"
  val testName: String = "testName"
  val testARN: String = "testARN"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.dashboard)
    .withSession(SessionKeys.name -> testName).withSession(SessionKeys.crn -> testCRN)

  val fakeRequestWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.dashboard)

  val fakeRequestArnSubmit: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.arnSumbit)

  val fakeRequestArnSubmitWithSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.arnSumbit)
    .withSession(SessionKeys.crn -> testCRN)

  val fakeRequestArnRemove: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.arnSumbit)

  val fakeRequestArnRemoveWithSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.arnSumbit)
    .withSession(SessionKeys.crn -> testCRN)

  "DashboardController dashboardMain() GET " should {
    "return status Ok" in {
      val result: Future[Result] = testDashboardController.dashboardMain(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include("Dashboard")
    }

    "return status Redirect" in {
      val result: Future[Result] = testDashboardController.dashboardMain(fakeRequestWithoutSession)

      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "DashboardController arnSubmit() POST " should {
    "return a error" when {
      "future is unsuccessful at checkArn" in {
        when(mockDataConnector.checkArn(any())) thenReturn Future.failed(new RuntimeException)
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequestArnSubmitWithSession.withFormUrlEncodedBody(agentFormField -> testARN)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }
      "future is unsuccessful at addArn" in {
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future.failed(new RuntimeException)
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequestArnSubmitWithSession.withFormUrlEncodedBody(agentFormField -> testARN)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }
    }

    "return Ok" when {
      "form without errors and the result returned from POST is 200 " in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmitWithSession
          .withFormUrlEncodedBody(agentFormField -> testARN)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(true)
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)

        status(result) shouldBe OK
      }
    }

    "return BadRequest" when {
      "nothing submitted in the form " in {
        val fakeRequestWithFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmit
          .withFormUrlEncodedBody(agentFormField -> "")
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithFormErrors
          .withSession(SessionKeys.crn -> testCRN))

        status(result) shouldBe BAD_REQUEST
        Jsoup.parse(contentAsString(result)).getElementById(agentFormField).`val` shouldBe ""
      }
    }

    "return BadRequest" when {
      "form without errors and the result returned from POST is 404" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmitWithSession
          .withFormUrlEncodedBody(agentFormField -> testARN)
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)

        status(result) shouldBe BAD_REQUEST
      }
    }

    "return not found" when {
      "arn doesn't exist" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmitWithSession
          .withFormUrlEncodedBody(agentFormField -> testARN)
        when(mockDataConnector.checkArn(any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)

        status(result) shouldBe NOT_FOUND
      }
    }

    "return Redirect" when {
      "session is empty(user is not logged in )" in {
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withFormUrlEncodedBody(agentFormField -> testARN))

        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

  "arnRemove() GET" should {
    "return error page" when {
      "arnRemomve future is unsuccessful" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemoveWithSession
          .withFormUrlEncodedBody(agentFormField -> testARN).withSession(SessionKeys.arn -> testARN)
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }
    }
    "return Ok" when {
      "correct input" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemoveWithSession
          .withFormUrlEncodedBody(agentFormField -> testARN).withSession(SessionKeys.arn -> testARN)
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(true)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)
        status(result) shouldBe OK
      }
    }

    "return Redirect" should {
      "not have a crn in session(user not logged in)" in {
        val result = testDashboardController.arnRemove(fakeRequestArnRemove)

        status(result) shouldBe Status.SEE_OTHER
      }
    }

    "return an error" should {
      "fail in the backend" in {
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(false)
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemoveWithSession.withFormUrlEncodedBody(agentFormField -> testARN).withSession(SessionKeys.arn -> testARN)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)

        status(result) shouldBe BAD_REQUEST
      }
      "no arn in session" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemoveWithSession.withFormUrlEncodedBody(agentFormField -> testARN)
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(false)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)

        status(result) shouldBe Status.NOT_FOUND
      }
    }
  }
}
