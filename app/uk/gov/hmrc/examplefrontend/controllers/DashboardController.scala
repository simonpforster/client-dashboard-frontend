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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, UserClientProperties, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, AgentForm}
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject()(mcc: MessagesControllerComponents,
                                    dashboardPage: DashboardPage,
                                    dataConnector: DataConnector,
                                    error: ErrorHandler,
                                    utils: Utils,
                                    implicit val ec: ExecutionContext)
  extends FrontendController(mcc) {

  def dashboardMain: Action[AnyContent] = Action async { implicit request =>
    utils.loggedInCheckAsync({ client =>
      Future(Ok(dashboardPage(client, AgentForm.form.fill(Agent("")))))
    })
  }

  def arnSubmit: Action[AnyContent] = Action async { implicit request =>
    utils.loggedInCheckAsync({ client =>
      val emptyForm: Form[Agent] = AgentForm.form.fill(Agent(""))
      val formWithErrors: Form[Agent] = AgentForm.form.fill(Agent("")).withGlobalError("NotFound")
      AgentForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(dashboardPage(client, formWithErrors)))
        },
        success => {
          dataConnector.checkArn(success).flatMap {
            case true => dataConnector.addArn(client.crn, success.arn).map {
              case true => Ok(dashboardPage(client = client.copy(arn = Some(success.arn)), agentForm = emptyForm))
                .withSession(request.session + (SessionKeys.arn -> success.arn))
              case false => BadRequest(dashboardPage(client = client, agentForm = formWithErrors))
                .withSession(request.session)
            }.recover {
              case _ => InternalServerError(error.standardErrorTemplate(
                pageTitle = ErrorMessages.pageTitle,
                heading = ErrorMessages.heading,
                message = ErrorMessages.message))
            }
            case false => Future.successful(NotFound(dashboardPage(client = client, agentForm = formWithErrors
              .withError(UserClientProperties.arn, "no"))))
          }.recover {
            case _ => InternalServerError(error.standardErrorTemplate(
              pageTitle = ErrorMessages.pageTitle,
              heading = ErrorMessages.heading,
              message = ErrorMessages.message))
          }
        })
    })
  }

  def arnRemove: Action[AnyContent] = Action.async { implicit request =>
    val emptyForm: Form[Agent] = AgentForm.form.fill(Agent(""))
    utils.loggedInCheckAsync({ client =>
      client.arn match {
        case Some(arn) =>
          dataConnector.removeArn(client.crn, arn).map {
            case true => Ok(dashboardPage(client = client.copy(arn = None), agentForm = emptyForm))
            case false => BadRequest(dashboardPage(client = client, agentForm = emptyForm))
          }.recover {
            case _ => InternalServerError(error.standardErrorTemplate(
              pageTitle = ErrorMessages.pageTitle,
              heading = ErrorMessages.heading,
              message = ErrorMessages.message))
          }
        case None => Future(NotFound(dashboardPage(client = client, agentForm = emptyForm))
          .withSession(request.session))
      }
    })
  }
}