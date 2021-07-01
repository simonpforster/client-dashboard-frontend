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
import org.mockito.Mockito.{mock, when}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html.{LoginPage, LogoutSuccess}

import scala.concurrent.{ExecutionContext, Future}

class LoginControllerSpec extends AbstractTest {
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit lazy val loginPage: LoginPage = app.injector.instanceOf[LoginPage]
  implicit lazy val logoutSuccess: LogoutSuccess = app.injector.instanceOf[LogoutSuccess]
  lazy val ws: WSClient = app.injector.instanceOf[WSClient]


  val fakeRequest = FakeRequest("GET", "/example-frontend/login")
  val wsclient = app.injector.instanceOf[WSClient]
  val connector = mock(classOf[DataConnector])
  val controller = new LoginController(wsclient, mcc, loginPage, connector,logoutSuccess,executionContext)

  val testClient: Client = Client("testCrn", "testName", "testBusiness", "testContact", 12, "testPostcode", "testBusinessType", Some("testArn"))
  val testClientJs = Json.parse(
    """{
      |  "crn": "testCrn",
      |  "name": "testName",
      |  "businessName": "testBusiness",
      |  "contactNumber": "testContact",
      |  "propertyNumber": 12,
      |  "postCode": "testPostCode",
      |  "businessType": "testBusinessType"
      |}""".stripMargin)

  "login() method GET" should {
    "return 200" in {
      val result = controller.login(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

    "logOut() method GET" should{
      "return 303" in {
        val result = controller.logOut(fakeRequest)
        status(result) shouldBe 200
        val doc=Jsoup.parse(contentAsString(result))
        Option(doc.getElementById("Logout-Success")).isDefined shouldBe true
      }
    }

    "return HTML" in {
      val result = controller.login(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
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
      when(connector.login(any())).thenReturn(Future.successful(Some(testClient)))

      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")

      val result = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe 303
      session(result).get("crn") shouldBe Some("testCrn")
    }

    "return Unauthorized" in {
      when(connector.login(any())).thenReturn(Future.successful(None))

      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")

      val result = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe UNAUTHORIZED
    }


    "return INTERNAL_SERVER_ERROR when userCredentials not correct" in {
      when(connector.login(any())).thenReturn(Future.failed(new Exception))

      val fakeRequestSubmit = fakeRequest.withFormUrlEncodedBody("crn" -> "test2", "password" -> "5678")

      lazy val result = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
