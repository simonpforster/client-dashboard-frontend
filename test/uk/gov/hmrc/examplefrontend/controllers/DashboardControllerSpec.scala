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

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, session, status}
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

import scala.concurrent.{ExecutionContext, Future}


class DashboardControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite  {
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  lazy val wsMock: WSClient = mock[WSClient]
  lazy val wsRequest: WSRequest = mock[WSRequest]
  lazy val wsResponse: WSResponse = mock[WSResponse]

  object testDashboardController extends DashboardController (
    wsMock,
    mcc,
    dashboardPage,
    executionContext
  )

  "DashboardController dashboardMain() GET " should {

    val fakeRequest = FakeRequest("GET", "/dashboard")
      .withSession("name" -> "John Doe" + "crn" -> "asd3748" )




    "return status Ok" in {
      val result = testDashboardController.dashboardMain(fakeRequest)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include ("Dashboard")
    }
  }

  "DashboardController clientName() GET " should {

    val jsonBody: JsObject = Json.obj (
      "name" -> "John Doe",
      "crn" -> "asd3748"
    )
    val fakeRequest = FakeRequest("GET", "/dash")

    "return redirect/SEE_OTHER " in {
      val result = testDashboardController.clientName(fakeRequest.withJsonBody(jsonBody))
      status(result) shouldBe Status.SEE_OTHER
      session(result).get("name") shouldBe Some("John Doe")
    }
  }


  "DashboardController arnSubmit() POST " should {

    val fakeRequestArnSubmit = FakeRequest("POST", "/arn-submit")

    "return BadRequest" when{
      "nothing submitted in the form " in {
        val fakeRequestWithFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "")
        val result = testDashboardController.arnSubmit(fakeRequestWithFormErrors)

        status(result) shouldBe BAD_REQUEST
        Jsoup.parse(contentAsString(result)).getElementById("arn").`val` shouldBe ""
      }
    }

    "return Ok" when{
      "form without errors and the result returned from POST is 200 " in {
        val fakeRequestWithFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")

        when(wsMock.url(ArgumentMatchers.any())) thenReturn wsRequest
        when(wsResponse.status) thenReturn 200
        when(wsResponse.json) thenReturn Json.parse(
          """{
            |  "arn": "testingArn"
            |}""".stripMargin)
        when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.successful(wsResponse)

        val result = testDashboardController.arnSubmit(fakeRequestWithFormErrors)

        status(result) shouldBe OK
      }
    }

    "return BadRequest" when{
      "form without errors and the result returned from POST is 404" in {
        val fakeRequestWithFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")

        when(wsMock.url(ArgumentMatchers.any())) thenReturn wsRequest
        when(wsResponse.status) thenReturn 404
        when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.successful(wsResponse)

        val result = testDashboardController.arnSubmit(fakeRequestWithFormErrors)

        status(result) shouldBe BAD_REQUEST
      }
    }

    "return InternalServerError" when{
      "something wrong with the response from server" in {
        val fakeRequestWithFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")

        when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.failed(new Throwable)
        val result = testDashboardController.arnSubmit(fakeRequestWithFormErrors)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
