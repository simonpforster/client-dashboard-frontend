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
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents, Result}
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

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = "/example-frontend/login")

  val fakeRequestWithSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = "/example-frontend/login").withSession("crn" -> "CRN3A1766D5")

  val connector: DataConnector = mock(classOf[DataConnector])
  val controller: LoginController = new LoginController(
    mcc = mcc,
    loginPage = loginPage,
    dataConnector = connector,
    logoutSuccessPage = logoutSuccess,
    ec = executionContext)

  val testClient: Client = Client(
    crn = "testCrn",
    name = "testName",
    businessName = "testBusiness",
    contactNumber = "testContact",
    propertyNumber = 12,
    postcode = "testPostcode",
    businessType = "testBusinessType",
    arn = Some("testArn"))

  val testClientJs: JsValue = Json.parse(
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
    "return 303" in {
      val result: Future[Result] = controller.login(fakeRequestWithSession)

      status(result) shouldBe Status.SEE_OTHER
    }

    "return 200" in {
      val result: Future[Result] = controller.login(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result: Future[Result] = controller.login(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "logOut() method GET" should {
    "return Ok" in {
      val result: Future[Result] = controller.logOut(fakeRequestWithSession)

      status(result) shouldBe Status.OK
      val doc: Document = Jsoup.parse(contentAsString(result))
      Option(doc.getElementById("Logout-Success")).isDefined shouldBe true
    }

    "return SEE_OTHER" in {
      val result: Future[Result] = controller.logOut(fakeRequest)

      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "loginSubmit() method POST" should {
    "return a failed page" in{
      when(connector.login(any())).thenReturn(Future.failed(new RuntimeException))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")
      val doc: Document = Jsoup.parse(contentAsString(result))
      doc.title() shouldBe "Something went wrong"
    }

    "return BadRequest when there are errors on the input fields" in {
      val fakeRequestWithFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody("crn" -> "", "password" -> "")
      lazy val result: Future[Result] = controller.loginSubmit(fakeRequestWithFormErrors)

      Jsoup.parse(contentAsString(result)).getElementById("crn").`val` shouldBe ""
      Jsoup.parse(contentAsString(result)).getElementById("password").`val` shouldBe ""
      status(result) shouldBe BAD_REQUEST
    }

    "redirect to the dashboard page with the corresponding session" in {
      when(connector.login(any())).thenReturn(Future.successful(Some(testClient)))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe 303
      session(result).get("crn") shouldBe Some("testCrn")
    }

    "return Unauthorized" in {
      when(connector.login(any())) thenReturn Future.successful(None)
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody("crn" -> "test", "password" -> "12345")
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe UNAUTHORIZED
    }

    "return INTERNAL_SERVER_ERROR when userCredentials not correct" in {
      when(connector.login(any())).thenReturn(Future.failed(new Exception))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody("crn" -> "test2", "password" -> "5678")
      lazy val result = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
