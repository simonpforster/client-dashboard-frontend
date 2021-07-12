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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.SessionKeys
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, AgentForm, Client}
import uk.gov.hmrc.examplefrontend.views.html.UpdateClientPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateClientController @Inject()(
                                        mcc: MessagesControllerComponents,
                                        updateClientPage: UpdateClientPage,
                                        error: ErrorHandler,
                                        implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def OpenUpdateClientPage: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val crn: String = request.session.get(SessionKeys.crn).getOrElse("")
      val name = request.session.get(SessionKeys.name).getOrElse("")
      val businessName = request.session.get(SessionKeys.businessName).getOrElse("")
      val contactNumber: String = request.session.get(SessionKeys.contactNumber).getOrElse("")
      val propertyNumber: String = request.session.get(SessionKeys.propertyNumber).getOrElse("")
      val postcode:String = request.session.get(SessionKeys.postcode).getOrElse("")
      val businessType: String = request.session.get(SessionKeys.businessType).getOrElse("")
      val arn: Option[String] = request.session.get(SessionKeys.arn)
      val clientOne = Client(crn, name, businessName, contactNumber, propertyNumber,postcode, businessType, arn)
      Future.successful(Ok(updateClientPage(clientOne)))
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }
}
