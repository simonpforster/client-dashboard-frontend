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
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UrlKeys, UserClientProperties, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object utils extends Utils(dataConnector = mockDataConnector, error = error)

  object testDashboardController extends DashboardController(
    mcc = mcc,
    dashboardPage = dashboardPage,
    dataConnector = mockDataConnector,
    error = error,
    utils = utils,
    ec = executionContext
  )

  val fakeRequestDashboard: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.dashboard)

  val fakeRequestArnSubmit: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "POST",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.arnSubmit)

  val fakeRequestArnRemove: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.arnRemove)

  "DashboardController dashboardMain() GET " should {
    "return status Ok" in {
      when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
      val result: Future[Result] = testDashboardController.dashboardMain(fakeRequestDashboard
        .withSession(SessionKeys.crn->testClient.crn))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(contentTypeMatch)
      contentAsString(result) should include("Dashboard")
    }
  }

  "DashboardController arnSubmit() POST " should {
    "return Ok" when {
      "form without errors and the result returned from POST is 200 " in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(true)
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.arn -> testARN))
        status(result) shouldBe OK
      }
    }
    "return a error" when {
      "future is unsuccessful at checkArn" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.checkArn(any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.arn -> testARN))
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }

      "future is unsuccessful at addArn" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.arn -> testARN))
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }
    }

    "return BadRequest" when {
      "nothing submitted in the form " in {
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withFormUrlEncodedBody(UserClientProperties.arn -> "")
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return BadRequest" when {
      "form without errors and the result returned from POST is 404" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.arn -> testARN))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return NotFound" when {
      "arn doesn't exist" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.checkArn(any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestArnSubmit
          .withSession(SessionKeys.crn -> testClient.crn)
          .withFormUrlEncodedBody(UserClientProperties.arn -> testARN))
        status(result) shouldBe NOT_FOUND
      }
    }
  }

  "arnRemove() GET" should {
    "return error page" when {
      "arnRemove future is unsuccessful" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result = testDashboardController.arnRemove(fakeRequestArnRemove
          .withSession(SessionKeys.crn -> testClient.crn))
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() shouldBe ErrorMessages.pageTitle
      }
    }
    "return Ok" when {
      "correct input" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(true)
        val result = testDashboardController.arnRemove(fakeRequestArnRemove
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe OK
      }
    }

    "return Not_Found" when {
      "no arn is client" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient.copy(arn = None)))
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(true)
        val result = testDashboardController.arnRemove(fakeRequestArnRemove
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe NOT_FOUND
      }
    }

    "return an error" should {
      "if remove arn returns error" in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnRemove(fakeRequestArnRemove
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe BAD_REQUEST
      }
      "if remove arn failed in the backend " in {
        when(mockDataConnector.readOne(any())) thenReturn Future(Some(testClient))
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future.failed(new RuntimeException)
        val result: Future[Result] = testDashboardController.arnRemove(fakeRequestArnRemove
          .withSession(SessionKeys.crn -> testClient.crn))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
