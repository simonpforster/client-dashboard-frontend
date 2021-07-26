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

package uk.gov.hmrc.examplefrontend.common

import play.api.mvc.Results.{BadRequest, InternalServerError, Redirect}
import play.api.mvc.{AnyContent, MessagesRequest, Result}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.controllers.routes
import uk.gov.hmrc.examplefrontend.models.Client

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Utils @Inject()(dataConnector: DataConnector,
                      error: ErrorHandler) {

  def loggedInCheckAsync(func: Client => Future[Result])(implicit request: MessagesRequest[AnyContent]): Future[Result] = {
    request.session.get(SessionKeys.crn) match {
      case Some(reg) =>
        dataConnector.readOne(reg).flatMap {
          case Some(client) => func(client)
          case None => Future(BadRequest(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message)))
        }.recover { case _ => InternalServerError(error.standardErrorTemplate(
          pageTitle = ErrorMessages.pageTitle,
          heading = ErrorMessages.heading,
          message = ErrorMessages.message))
        }
      case None => Future(Redirect(routes.HomePageController.homepage()))
    }
  }

  def loggedInCheckNoClient(request: MessagesRequest[AnyContent],
                            func: String => Future[Result]): Future[Result] = {
    request.session.get(SessionKeys.crn) match {
      case Some(reg) => func(reg)
      case None => Future(Redirect(routes.HomePageController.homepage()))
    }
  }

  def notLoggedInCheck(request: MessagesRequest[AnyContent],
                       func: String => Future[Result]): Future[Result] = {
    request.session.get(SessionKeys.crn) match {
      case Some(reg) => Future(Redirect(routes.DashboardController.dashboardMain()))
      case None => func("")
    }
  }
}
