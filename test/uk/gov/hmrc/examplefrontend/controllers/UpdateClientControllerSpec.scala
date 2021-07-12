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

import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, contentType, defaultAwaitTimeout, status}
import uk.gov.hmrc.examplefrontend.common.{SessionKeys, UrlKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.Client
import uk.gov.hmrc.examplefrontend.views.html.{DashboardPage, UpdateClientPage, UpdateClientPropertyPage}

import scala.concurrent.{ExecutionContext, Future}

class UpdateClientControllerSpec extends AbstractTest {


  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  lazy val updateClientPage: UpdateClientPage = app.injector.instanceOf[UpdateClientPage]
  lazy val mockDataConnector: DataConnector = mock[DataConnector]
  val error: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  implicit lazy val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  lazy val mockupdateClientPropertyPage:UpdateClientPropertyPage = app.injector.instanceOf[UpdateClientPropertyPage]

  object testUpdateClientController extends UpdateClientController(
    mcc = mcc,
    updateClientPage = updateClientPage,
    error = error,
    ec = executionContext,
    updateClientPropertyPage = mockupdateClientPropertyPage
  )

  private val client: Client = Client(
    crn = "CRN",
    name = "TestFullName",
    businessName = "TestNameOfBusiness",
    contactNumber = "01111111111111",
    propertyNumber = "10",
    postcode = "TestAddress",
    businessType = "Private Limited",
    arn = Option("Arn"))

  val fakeRequestProperty: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClientProperty
  )

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)
    .withSession(
      SessionKeys.name -> client.name,
      SessionKeys.businessName -> client.businessName,
      SessionKeys.contactNumber -> client.contactNumber,
      SessionKeys.postcode -> client.postcode,
      SessionKeys.propertyNumber-> client.propertyNumber,
      SessionKeys.businessType -> client.businessType,
      SessionKeys.crn -> client.crn)

  val fakeRequestWithoutSession: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(
    method = "GET",
    path = UrlKeys.modifyClient)

  "ModifyClientController() GET " should {
    "return status Ok" in {
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      contentAsString(result) should include("Modify Account")
    }

    "return status Redirect" in {
      val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestWithoutSession)
      status(result) shouldBe Status.SEE_OTHER
    }
  }
  "return status OK" in {
    val result: Future[Result] = testUpdateClientController.OpenUpdateClientProperty(fakeRequestProperty)

    status(result) shouldBe Status.OK
    contentType(result) shouldBe Some("text/html")
    contentAsString(result) should include("Update Property")
  }
  "return status Redirect" in {
    val result: Future[Result] = testUpdateClientController.OpenUpdateClientPage(fakeRequestWithoutSession)
    status(result) shouldBe Status.SEE_OTHER
  }
}
