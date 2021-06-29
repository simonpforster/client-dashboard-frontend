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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, session, status}
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

class DashboardControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite  {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]

  object testDashboardController extends DashboardController (
    mcc,
    dashboardPage
  )

  "HomeController dashboardMain() GET " should {
    val fakeRequest = FakeRequest("GET", "/")

    "return status Ok" in {
      val result = testDashboardController.dashboardMain(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "HomeController clientName() GET " should {

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

}
