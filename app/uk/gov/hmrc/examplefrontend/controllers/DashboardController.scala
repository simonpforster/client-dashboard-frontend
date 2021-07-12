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
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.SessionKeys
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, AgentForm, Client}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject()(mcc: MessagesControllerComponents,
                                    dashboardPage: DashboardPage,
                                    dataConnector: DataConnector,
                                    error: ErrorHandler,
                                    implicit val ec: ExecutionContext)
  extends FrontendController(mcc) {

  def dashboardMain: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val clientOne = Client(request.session.get(SessionKeys.crn).getOrElse(""),
        request.session.get(SessionKeys.name).getOrElse(""),
        request.session.get(SessionKeys.businessName).getOrElse(""),
        request.session.get(SessionKeys.contactNumber).getOrElse(""),
        request.session.get(SessionKeys.propertyNumber).getOrElse(""),
        request.session.get(SessionKeys.postcode).getOrElse(""),
        request.session.get(SessionKeys.businessType).getOrElse(""),
        request.session.get(SessionKeys.arn))
      Future.successful(Ok(dashboardPage(clientOne, AgentForm.form.fill(Agent("")))))
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }

  def arnSubmit: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val clientOne = Client(request.session.get(SessionKeys.crn).getOrElse(""),
        request.session.get(SessionKeys.name).getOrElse(""),
        request.session.get(SessionKeys.businessName).getOrElse(""),
        request.session.get(SessionKeys.contactNumber).getOrElse(""),
        request.session.get(SessionKeys.propertyNumber).getOrElse(""),
        request.session.get(SessionKeys.postcode).getOrElse(""),
        request.session.get(SessionKeys.businessType).getOrElse(""),
        request.session.get(SessionKeys.arn))
      val emptyForm: Form[Agent] = AgentForm.form.fill(Agent(""))
      val formWithErrors: Form[Agent] = AgentForm.form.fill(Agent("")).withGlobalError("NotFound")
      AgentForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(dashboardPage(clientOne, formWithErrors)))
        },
        success => {
          dataConnector.checkArn(success).flatMap {
            case true => dataConnector.addArn(clientOne, success).map {
              case true => Ok(dashboardPage(client = clientOne.copy(arn = Some(success.arn)), agentForm = emptyForm))
                .withSession(request.session + ("clientArn" -> success.arn))
              case false => BadRequest(dashboardPage(client = clientOne, agentForm = formWithErrors))
                .withSession(request.session)
            }.recover {
              case _ => InternalServerError(error.standardErrorTemplate(
                pageTitle = "Something went wrong",
                heading = "Something went wrong",
                message = "Come back later"))
            }
            case false => Future.successful(NotFound(dashboardPage(client = clientOne, agentForm = formWithErrors
              .withError("arn", "no"))))
          }.recover {
            case _ => InternalServerError(error.standardErrorTemplate(
              pageTitle = "Something went wrong",
              heading = "Something went wrong",
              message = "Come back later"))
          }
        }
      )
    } else {
      Future(Redirect(routes.HomePageController.homepage()))
    }

  }

  def arnRemove: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val clientOne = Client(request.session.get(SessionKeys.crn).getOrElse(""),
        request.session.get(SessionKeys.name).getOrElse(""),
        request.session.get(SessionKeys.businessName).getOrElse(""),
        request.session.get(SessionKeys.contactNumber).getOrElse(""),
        request.session.get(SessionKeys.propertyNumber).getOrElse(""),
        request.session.get(SessionKeys.postcode).getOrElse(""),
        request.session.get(SessionKeys.businessType).getOrElse(""),
        request.session.get(SessionKeys.arn))
      val emptyForm: Form[Agent] = AgentForm.form.fill(Agent(""))
      clientOne.arn match {
        case Some(arn) =>
          dataConnector.removeArn(clientOne, Agent(arn)).map {
            case true => Ok(dashboardPage(client = clientOne.copy(arn = None), agentForm = emptyForm))
              .withSession(request.session - SessionKeys.arn)
            case false => BadRequest(dashboardPage(client = clientOne, agentForm = emptyForm))
              .withSession(request.session)
          }.recover {
            case _ => InternalServerError(error.standardErrorTemplate(
              pageTitle = "Something went wrong",
              heading = "Something went wrong",
              message = "Delete arn unsuccessful"))
          }
        case None => Future(NotFound(dashboardPage(client = clientOne, agentForm = emptyForm))
          .withSession(request.session))
      }
    } else {
      Future(Redirect(routes.HomePageController.homepage()))
    }
  }
}
