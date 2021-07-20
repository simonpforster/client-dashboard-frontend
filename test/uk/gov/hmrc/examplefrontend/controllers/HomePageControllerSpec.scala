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

import play.api.http.Status
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{charset, contentType, defaultAwaitTimeout, status}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.helpers.AbstractTest

import scala.concurrent.Future

class HomePageControllerSpec extends AbstractTest {

  val testCRN = "test"
  private val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.clients)
  val contentTypeMatch: String = "text/html"
  val charsetMatch: String = "utf-8"
  private val fakeRequestWithSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.clients)
    .withSession(SessionKeys.crn -> testCRN)
  private val fakeRequestReg = FakeRequest(
    method = "GET",
    path = UrlKeys.registrationNI
  )

  private val controller = app.injector.instanceOf[HomePageController]

  "homepage() method" should {
    "return Ok" in {
      val result: Future[Result] = controller.homepage(fakeRequest)
      status(result) shouldBe OK
    }

    "return SEE_OTHER" in {
      val result: Future[Result] = controller.homepage(fakeRequestWithSession)

      status(result) shouldBe SEE_OTHER
    }

    "return HTML" in {
      val result: Future[Result] = controller.homepage(fakeRequest)
      contentType(result) shouldBe Some(contentTypeMatch)
      charset(result) shouldBe Some(charsetMatch)
    }
  }

  "registration() method" should {
    "return SEE_OTHER" in {
      val result: Future[Result] = controller.registration(fakeRequestReg)
      status(result) shouldBe SEE_OTHER
    }
  }
}
