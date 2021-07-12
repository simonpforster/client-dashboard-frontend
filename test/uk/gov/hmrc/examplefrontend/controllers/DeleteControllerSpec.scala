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
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.Helpers.{charset, contentAsString, contentType, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.views.html.{DashboardPage, DeleteAreYouSure, DeleteSuccess}

import scala.concurrent.{ExecutionContext, Future}

class DeleteControllerSpec extends AbstractTest {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm" -> false,
        "metrics.enabled" -> false
      )
      .build()

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]
  lazy val deleteSuccessPage: DeleteSuccess = app.injector.instanceOf[DeleteSuccess]
  lazy val areYouSure: DeleteAreYouSure = app.injector.instanceOf[DeleteAreYouSure]
  lazy val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  val dataConnector: DataConnector = mock(classOf[DataConnector])

  private val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.deleteSelect)

  private val controller = new DeleteClientController(
    mcc = Helpers.stubMessagesControllerComponents(),
    dataConnector = dataConnector,
    deleteSuccess = deleteSuccessPage,
    deleteAreYouSure = areYouSure,
    error = error,
    ec = ec)

  val crn: String = "CRNC5D7C333"
  val contentTypeMatch: String = "text/html"
  val charsetMatch: String = "utf-8"
  "areYouSure()" should {
    "return 200" in {
      val result: Future[Result] = controller.areYouSure()
        .apply(fakeRequest.withSession(SessionKeys.crn -> crn))
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result: Future[Result] = controller.areYouSure()
        .apply(fakeRequest.withSession(SessionKeys.crn -> crn))
      contentType(result) shouldBe Some(contentTypeMatch)
      charset(result) shouldBe Some(charsetMatch)
    }

    "return 303" in {
      val result: Future[Result] = controller.areYouSure().apply(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "deleteClient()" should {
    "delete future fail" in {
      when(dataConnector.deleteClient(any())) thenReturn Future.failed(new RuntimeException)
      val result: Future[Result] = controller.deleteClient().apply(fakeRequest
        .withSession(SessionKeys.crn -> crn))
      val doc: Document = Jsoup.parse(contentAsString(result))
      doc.title() shouldBe ErrorMessages.pageTitle
    }

    "delete" in {
      when(dataConnector.deleteClient(any())) thenReturn Future(true)
      val result: Future[Result] = controller.deleteClient().apply(fakeRequest
        .withSession(SessionKeys.crn -> crn))
      status(result) shouldBe Status.SEE_OTHER
    }

    "delete without crn (unsuccessful) redirects" in {
      val result: Future[Result] = controller.deleteClient().apply(fakeRequest.withSession())
      status(result) shouldBe Status.SEE_OTHER
    }

    "delete (unsuccessfully)" in {
      when(dataConnector.deleteClient(any())) thenReturn Future(false)
      val result: Future[Result] = controller.deleteClient().apply(fakeRequest
        .withSession(SessionKeys.crn -> crn))
      status(result) shouldBe Status.BAD_GATEWAY
    }

  }

  "deleteClientSuccessful()" should {
    "return 200" in {
      val result: Future[Result] = controller.deleteClientSuccessful().apply(fakeRequest
        .withSession(SessionKeys.crn -> crn))
      status(result) shouldBe Status.OK
    }

    "return 303" in {
      val result: Future[Result] = controller.deleteClientSuccessful().apply(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
    }

    "return HTML" in {
      val result: Future[Result] = controller.deleteClientSuccessful().apply(fakeRequest
        .withSession(SessionKeys.crn -> crn))
      contentType(result) shouldBe Some(contentTypeMatch)
      charset(result) shouldBe Some(charsetMatch)
    }
  }
}


