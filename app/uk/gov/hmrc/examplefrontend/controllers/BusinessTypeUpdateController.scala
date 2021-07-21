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
import uk.gov.hmrc.examplefrontend.models.{Client, UserBusinessType, UserBusinessTypeForm}
import uk.gov.hmrc.examplefrontend.views.html.UpdateBusinessTypePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessTypeUpdateController @Inject()(
                                              mcc: MessagesControllerComponents,
                                              updateBusinessTypePage: UpdateBusinessTypePage,
                                              dataConnector: DataConnector,
                                              error: ErrorHandler,
                                              implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def updateBusinessType(): Action[AnyContent] = Action { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val form = UserBusinessTypeForm.submitForm.fill(UserBusinessType(""))
      val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
      Ok(updateBusinessTypePage(form, registeredClient))
    } else Redirect(routes.LoginController.login())
  }

  def submitBusinessTypeUpdate: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
      UserBusinessTypeForm.submitForm.bindFromRequest().fold(formWithErrors =>
        Future.successful(BadRequest(updateBusinessTypePage(formWithErrors, registeredClient))),
        success => {
          dataConnector.updateBusinessType(request.session.get(SessionKeys.crn).get, success.businessType).map {
            case true => Redirect(routes.UpdateClientController.openUpdateClientPage())
            case false => NotImplemented(error.standardErrorTemplate(
              pageTitle = ErrorMessages.pageTitle,
              heading = ErrorMessages.heading,
              message = ErrorMessages.message
            ))
          }.recover {
            case _ => InternalServerError(error.standardErrorTemplate(
              pageTitle = ErrorMessages.pageTitle,
              heading = ErrorMessages.heading,
              message = ErrorMessages.message))
          }
        }
      )
    } else {
      Future(Redirect(routes.HomePageController.homepage()))
    }
  }

}
