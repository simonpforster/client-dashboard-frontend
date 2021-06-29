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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.examplefrontend.views.html.LoginPage

import scala.concurrent.{ExecutionContext, Future}

class LoginControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val loginPage: LoginPage = app.injector.instanceOf[LoginPage]
  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  lazy val wsMock: WSClient = mock[WSClient]
  lazy val wsRequest: WSRequest = mock[WSRequest]
  lazy val wsResponse: WSResponse = mock[WSResponse]

  private val fakeRequest = FakeRequest("GET", "/example-frontend/login")
  private val controller = app.injector.instanceOf[LoginController]

  "login() method GET" should {
    "return 200" in {
      val result = controller.login(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.login(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }


  "loginSubmit() method POST" should {

    "return BadRequest when there are errors on the input fields" in {
      val fakeRequestWithFormErrors = fakeRequest.withFormUrlEncodedBody("crn" -> "", "password" -> "")
      lazy val result = controller.loginSubmit(fakeRequestWithFormErrors)

      Jsoup.parse(contentAsString(result)).getElementById("crn").`val` shouldBe ""
      Jsoup.parse(contentAsString(result)).getElementById("password").`val` shouldBe ""
      status(result) shouldBe BAD_REQUEST
    }

    "redirect to the dashboard page with the corresponding session" in {
      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")
      lazy val newController = new LoginController(wsMock, mcc, loginPage, executionContext)
      lazy val result = newController.loginSubmit(fakeRequestSubmit)

      when(wsMock.url(ArgumentMatchers.any())) thenReturn wsRequest
      when(wsResponse.status) thenReturn 200
      when(wsResponse.json) thenReturn Json.parse(
        """{
          |  "crn": "testCrn",
          |  "name": "testName",
          |  "businessName": "testBusiness",
          |  "contactNumber": "testContact",
          |  "propertyNumber": 12,
          |  "postCode": "testPostCode",
          |  "businessType": "testBusinessType"
          |}""".stripMargin)
      when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.successful(wsResponse)

      status(result) shouldBe 303
      session(result).get("crn") shouldBe Some("testCrn")
    }

    "redirect to the login page" in {
      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")
      lazy val newController = new LoginController(wsMock, mcc, loginPage, executionContext)
      lazy val result = newController.loginSubmit(fakeRequestSubmit)

      when(wsMock.url(ArgumentMatchers.any())) thenReturn wsRequest
      when(wsResponse.status) thenReturn 401
      when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.successful(wsResponse)

      status(result) shouldBe 401
    }


    "return INTERNAL_SERVER_ERROR when userCredentials not correct" in {
      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test2", "password" -> "5678")
      lazy val newController = new LoginController(wsMock, mcc, loginPage, executionContext)
      lazy val result = newController.loginSubmit(fakeRequestSubmit)

      when(wsMock.url(ArgumentMatchers.any())) thenReturn wsRequest
      when(wsResponse.status) thenReturn 400
      when(wsRequest.post(any[JsObject]())(any[BodyWritable[JsObject]]())) thenReturn Future.failed(new Throwable)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      session(result).get("username") shouldBe None
    }
  }
}
