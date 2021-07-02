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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, session, status}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Agent
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage

import scala.concurrent.{ExecutionContext, Future}


class DashboardControllerSpec extends AbstractTest {
  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val dashboardPage: DashboardPage = app.injector.instanceOf[DashboardPage]
  lazy val mockDataConnector = mock[DataConnector]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  object testDashboardController extends DashboardController (
    mcc,
    dashboardPage,
    mockDataConnector,
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
        val fakeRequestWithoutFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")
        val testObj = Agent("test")
        when(mockDataConnector.addArn(any())).thenReturn (Future(Some(testObj)))
        val result = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)
        status(result) shouldBe OK
      }
    }

    "return BadRequest" when{
      "form without errors and the result returned from POST is 404" in {
        val fakeRequestWithoutFormErrors = fakeRequestArnSubmit.withFormUrlEncodedBody("arn" -> "testingArn")

        when(mockDataConnector.addArn(any())).thenReturn (Future(None))
        val result = testDashboardController.arnSubmit(fakeRequestWithoutFormErrors)

        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
