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
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html.{LoginPage, LogoutSuccess}

import scala.concurrent.{ExecutionContext, Future}

class LoginControllerSpec extends AbstractTest {
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val loginPage: LoginPage = app.injector.instanceOf[LoginPage]
  implicit lazy val logoutSuccess: LogoutSuccess = app.injector.instanceOf[LogoutSuccess]
  implicit lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  val testClient: Client = Client(
    crn = "testCrn",
    name = "testName",
    businessName = "testBusiness",
    contactNumber = "testContact",
    propertyNumber = "12",
    postcode = "testPostcode",
    businessType = "testBusinessType",
    arn = Some("testArn"))
  val testPass: String = "12345"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.clientLogin)

  val fakeRequestWithSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.clientLogin).withSession(SessionKeys.crn -> testClient.crn)

  val connector: DataConnector = mock(classOf[DataConnector])
  val controller: LoginController = new LoginController(
    mcc = mcc,
    loginPage = loginPage,
    dataConnector = connector,
    logoutSuccessPage = logoutSuccess,
    error = error,
    ec = executionContext)

  val contentTypeMatch: String = "text/html"
  val charsetMatch: String = "utf-8"

  val testClientJs: JsValue = Json.parse(
    s"""{
       |  "${UserClientProperties.crn}": "${testClient.crn}",
       |  "${UserClientProperties.name}": "${testClient.name}",
       |  "${UserClientProperties.businessName}": "${testClient.businessName}",
       |  "${UserClientProperties.contactNumber}": "${testClient.contactNumber}",
       |  "${UserClientProperties.propertyNumber}": ${testClient.propertyNumber},
       |  "${UserClientProperties.postcode}": "${testClient.postcode}",
       |  "${UserClientProperties.businessType}": "${testClient.businessType}"
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

      contentType(result) shouldBe Some(contentTypeMatch)
      charset(result) shouldBe Some(charsetMatch)
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
    "return a failed page" in {
      when(connector.login(any())).thenReturn(Future.failed(new RuntimeException))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPass)
      val result = controller.loginSubmit(fakeRequestSubmit)
      val doc: Document = Jsoup.parse(contentAsString(result))
      doc.title() shouldBe ErrorMessages.pageTitle
    }

    "return BadRequest when there are errors on the input fields" in {
      val fakeRequestWithFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody(UserClientProperties.crn -> "", UserClientProperties.password -> "")
      lazy val result: Future[Result] = controller.loginSubmit(fakeRequestWithFormErrors)

      Jsoup.parse(contentAsString(result)).getElementById("crn").`val` shouldBe ""
      Jsoup.parse(contentAsString(result)).getElementById("password").`val` shouldBe ""
      status(result) shouldBe BAD_REQUEST
    }

    "redirect to the dashboard page with the corresponding session" in {
      when(connector.login(any())).thenReturn(Future.successful(Some(testClient)))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPass)
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe 303
      session(result).get(SessionKeys.crn) shouldBe Some(testClient.crn)
    }

    "return Unauthorized" in {
      when(connector.login(any())) thenReturn Future.successful(None)
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequest
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPass)
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)

      status(result) shouldBe UNAUTHORIZED
    }
  }
}
