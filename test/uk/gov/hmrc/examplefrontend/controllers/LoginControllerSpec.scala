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
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UrlKeys, UserClientProperties, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest
import uk.gov.hmrc.examplefrontend.views.html.{HomePage, LoginPage}

import scala.concurrent.{ExecutionContext, Future}

class LoginControllerSpec extends AbstractTest {
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit lazy val loginPage: LoginPage = app.injector.instanceOf[LoginPage]
  implicit lazy val home: HomePage = app.injector.instanceOf[HomePage]
  implicit lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val utils: Utils = app.injector.instanceOf[Utils]
  val testPass: String = "12345"

  val connector: DataConnector = mock(classOf[DataConnector])

  val controller: LoginController = new LoginController(
    mcc = mcc,
    loginPage = loginPage,
    dataConnector = connector,
    home = home,
    error = error,
    utils = utils,
    ec = executionContext)

  val fakeRequestLogin: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.login)

  val fakeRequestLogout: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.host + UrlKeys.client + UrlKeys.logOut)

  "login() method GET" should {
    "return 303" in {
      val result: Future[Result] = controller.login(fakeRequestLogin.withSession(SessionKeys.crn -> testClient.crn))
      status(result) shouldBe SEE_OTHER
    }

    "return 200" in {
      val result: Future[Result] = controller.login(fakeRequestLogin)
      status(result) shouldBe OK
    }

    "return HTML" in {
      val result: Future[Result] = controller.login(fakeRequestLogin)
      contentType(result) shouldBe Some(contentTypeMatch)
      charset(result) shouldBe Some(charsetMatch)
    }
  }

  "logOut() method GET" should {
    "return Ok" in {
      val result: Future[Result] = controller.logOut(fakeRequestLogout.withSession(SessionKeys.crn -> testClient.crn))
      status(result) shouldBe OK
      val doc: Document = Jsoup.parse(contentAsString(result))
      Option(doc.getElementById("home")).isDefined shouldBe true
    }

    "return SEE_OTHER" in {
      val result: Future[Result] = controller.logOut(fakeRequestLogout)
      status(result) shouldBe SEE_OTHER
    }
  }

  "loginSubmit() method POST" should {
    "return a failed page" in {
      when(connector.login(any())).thenReturn(Future.failed(new RuntimeException))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestLogin
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPassword)
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)
      val doc: Document = Jsoup.parse(contentAsString(result))
      doc.title() shouldBe ErrorMessages.pageTitle
    }

    "return BadRequest when there are errors on the input fields" in {
      val fakeRequestWithFormErrors: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestLogin
        .withFormUrlEncodedBody(UserClientProperties.crn -> "", UserClientProperties.password -> "")
      lazy val result: Future[Result] = controller.loginSubmit(fakeRequestWithFormErrors)
      Jsoup.parse(contentAsString(result)).getElementById("crn").`val` shouldBe ""
      Jsoup.parse(contentAsString(result)).getElementById("password").`val` shouldBe ""
      status(result) shouldBe BAD_REQUEST
    }

    "redirect to the dashboard page with the corresponding session" in {
      when(connector.login(any())).thenReturn(Future.successful(Some(testClient)))
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestLogin
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPassword)
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)
      status(result) shouldBe SEE_OTHER
      session(result).get(SessionKeys.crn) shouldBe Some(testClient.crn)
    }

    "return Unauthorized" in {
      when(connector.login(any())) thenReturn Future.successful(None)
      val fakeRequestSubmit: FakeRequest[AnyContentAsFormUrlEncoded] = fakeRequestLogin
        .withFormUrlEncodedBody(UserClientProperties.crn -> testClient.crn, UserClientProperties.password -> testPassword)
      val result: Future[Result] = controller.loginSubmit(fakeRequestSubmit)
      status(result) shouldBe UNAUTHORIZED
    }
  }
}
