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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, session, status}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

import scala.concurrent.{ExecutionContext, Future}


class DashboardControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object testDashboardController extends DashboardController(
    mcc,
    dashboardPage,
    mockDataConnector,
    executionContext
  )

  "DashboardController dashboardMain() GET " should {

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
      method = "GET",
      path = "/dashboard")
      .withSession("name" -> "John Doe" + "crn" -> "asd3748")


    "return status Ok" in {
      val result: Future[Result] = testDashboardController.dashboardMain(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include("Dashboard")
    }
  }

  "DashboardController clientName() GET " should {

    val jsonBody: JsObject = Json.obj(
      "name" -> "John Doe",
      "crn" -> "asd3748"
    )
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
      method = "GET",
      path = "/dash")

    "return redirect/SEE_OTHER " in {
      val result: Future[Result] = testDashboardController.clientName(fakeRequest.withJsonBody(jsonBody))
      status(result) shouldBe Status.SEE_OTHER
      session(result).get("name") shouldBe Some("John Doe")
    }
  }


  "DashboardController arnSubmit() POST " should {

    val fakeRequestArnSubmit: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
      method = "POST",
      path = "/arn-submit")


    "return Ok" when {
      "form without errors and the result returned from POST is 200 " in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn").withSession("crn" -> "testCrn")
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(true)
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)
        status(result) shouldBe OK
      }
    }

    "return BadRequest" when {
      "nothing submitted in the form " in {
        val fakeRequestWithFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "")
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithFormErrors)
        status(result) shouldBe BAD_REQUEST
        Jsoup.parse(contentAsString(result)).getElementById("arn").`val` shouldBe ""
      }
    }



    "return BadRequest" when {
      "form without errors and the result returned from POST is 404" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")
        when(mockDataConnector.checkArn(any())) thenReturn Future(true)
        when(mockDataConnector.addArn(any(), any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors.withSession("crn" -> "testCrn"))

        status(result) shouldBe BAD_REQUEST
      }
    }
    "return not found" when{
      "arn doesn't exist" in{
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")
        when(mockDataConnector.checkArn(any())) thenReturn Future(false)
        val result: Future[Result] = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors.withSession("crn" -> "testCrn"))

        status(result) shouldBe NOT_FOUND
      }
    }
  }

  "arnRemove() GET" can {
    val fakeRequestArnRemove: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/arn-submit")

    "return Ok" should {
      "correct input" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemove.withFormUrlEncodedBody("arn" -> "testingArn").withSession("crn" -> "testCrn").withSession("arn" -> "testArn")
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(true)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)

        status(result) shouldBe OK
      }
    }

    "return an error" should {
      "fail in the backend" in {
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemove.withFormUrlEncodedBody("arn" -> "testingArn").withSession("crn" -> "testCrn")
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(false)
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)

        status(result) shouldBe 404
      }
      "no arn in session" in {
        when(mockDataConnector.removeArn(any(), any())) thenReturn Future(false)
        val fakeRequestWithoutFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestArnRemove.withFormUrlEncodedBody("arn" -> "testingArn").withSession("crn" -> "testCrn").withSession("arn" -> "testArn")
        val result = testDashboardController.arnRemove(fakeRequestWithoutFormErrors)

        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
