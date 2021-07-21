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

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{User, UserForm}
import uk.gov.hmrc.examplefrontend.views.html.{LoginPage, LogoutSuccess}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()(
                                 mcc: MessagesControllerComponents,
                                 loginPage: LoginPage,
                                 dataConnector: DataConnector,
                                 logoutSuccessPage: LogoutSuccess,
                                 error: ErrorHandler,
                                 utils: Utils,
                                 implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def login: Action[AnyContent] = Action async { implicit request =>
    val form: Form[User] = UserForm.form.fill(User(crn = "", password = ""))
    utils.notLoggedInCheck(request, _ => Future(Ok(loginPage(form))))
  }

  def logOut: Action[AnyContent] = Action async { implicit request =>
    utils.loggedInCheckNoClient(request, _ =>
      Future(Ok(logoutSuccessPage()).withNewSession)
    )
  }

  def loginSubmit: Action[AnyContent] = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(loginPage(formWithErrors)))
      }, success => {
        dataConnector.login(success).map {
          case Some(client) =>
            Redirect(routes.DashboardController.dashboardMain()).withNewSession.withSession(SessionKeys.crn -> client.crn)
          case None => Unauthorized(loginPage(UserForm.form.fill(User("", ""))))
        }.recover {
          case _ => InternalServerError(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message))
        }
      }
    )
  }
}
