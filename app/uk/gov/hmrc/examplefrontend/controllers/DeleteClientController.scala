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
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{CRN, Client}
import uk.gov.hmrc.examplefrontend.views.html.{DeleteAreYouSure, DeleteSuccess}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteClientController @Inject()(mcc: MessagesControllerComponents,
                                       dataConnector: DataConnector,
                                       deleteSuccess: DeleteSuccess,
                                       deleteAreYouSure: DeleteAreYouSure,
                                       error: ErrorHandler,
                                       implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def deleteClient(): Action[AnyContent] = Action async { implicit request =>
    request.session.get(SessionKeys.crn) match {
      case Some(value) =>
        val crn = CRN(value)
        val response: Future[Boolean] = dataConnector.deleteClient(crn)
        response.map {
          case true => Redirect(routes.DeleteClientController.deleteClientSuccessful())
          case false => Redirect(routes.DashboardController.dashboardMain(), BAD_GATEWAY)
        }.recover {
          case _ => InternalServerError(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message))
        }
      case None => Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }

  def areYouSure(): Action[AnyContent] = Action { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val clientOne = Client(request.session.get(SessionKeys.crn).getOrElse(""),
        request.session.get(SessionKeys.name).getOrElse(""),
        request.session.get(SessionKeys.businessName).getOrElse(""),
        request.session.get(SessionKeys.contactNumber).getOrElse(""),
        request.session.get(SessionKeys.propertyNumber).getOrElse(""),
        request.session.get(SessionKeys.postcode).getOrElse(""),
        request.session.get(SessionKeys.businessType).getOrElse(""),
        request.session.get(SessionKeys.arn))
      Ok(deleteAreYouSure(clientOne))
    } else {
      Redirect(routes.HomePageController.homepage())
    }

  }

  def deleteClientSuccessful(): Action[AnyContent] = Action { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      Ok(deleteSuccess()).withNewSession
    } else {
      Redirect(routes.HomePageController.homepage())
    }
  }
}
