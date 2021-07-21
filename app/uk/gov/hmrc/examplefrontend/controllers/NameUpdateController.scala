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
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{UserName, UserNameForm}
import uk.gov.hmrc.examplefrontend.views.html.UpdateNamePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NameUpdateController@Inject()(
                                     mcc: MessagesControllerComponents,
                                     nameUpdatePage: UpdateNamePage,
                                     dataConnector: DataConnector,
                                     error: ErrorHandler)
  extends FrontendController(mcc) with I18nSupport{

  def updateName(): Action[AnyContent] = Action { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      Ok(nameUpdatePage(UserNameForm.submitForm.fill(UserName(""))))
    } else {
      Redirect(routes.HomePageController.homepage())
    }
  }

  def updateNameSubmit(): Action[AnyContent] = Action.async { implicit request =>
    Utils.loggedInCheckAsync(request, crn => {
      UserNameForm.submitForm.bindFromRequest.fold({ formWithErrors =>
        Future(BadRequest(nameUpdatePage(formWithErrors)))
      },{ success =>
        dataConnector.updateClientName(crn, success.name).map {
          case true => Redirect(routes.UpdateClientController.openUpdateClientPage())
          case false => ServiceUnavailable(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message))
        }.recover {
          case _ => NotFound(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message))
        }
      })
    })
  }

}
