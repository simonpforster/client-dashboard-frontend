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

import uk.gov.hmrc.examplefrontend.views.html.DashboardPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Agent, AgentForm, Client}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject()(
                                     mcc: MessagesControllerComponents,
                                     dashboardPage: DashboardPage,
                                     dataConnector: DataConnector,
                                     implicit val ec: ExecutionContext
                                      )
  extends FrontendController(mcc){

  def dashboardMain: Action[AnyContent] = Action.async { implicit request =>
    val clientOne = Client(request.session.get("crn").getOrElse(""),
                          request.session.get("name").getOrElse(""),
                          request.session.get("businessName").getOrElse(""),
                          request.session.get("contactNumber").getOrElse(""),
                          (request.session.get("propertyNumber").getOrElse("1")).toInt,
                          request.session.get("postcode").getOrElse(""),
                          request.session.get("businessType").getOrElse(""),
                          request.session.get("arn"))

    Future.successful(Ok(dashboardPage(clientOne, AgentForm.form.fill(Agent("")))))
  }

  def clientName: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.DashboardController.dashboardMain())
      .withSession(request.session + ("name" -> "John Doe" ) + ("crn" -> "asd39402" ))
  }

  def arnSubmit: Action[AnyContent] = Action.async { implicit request =>
    val clientOne = Client(request.session.get("crn").getOrElse(""),
                          request.session.get("name").getOrElse(""),
                          request.session.get("businessName").getOrElse(""),
                          request.session.get("contactNumber").getOrElse(""),
                          (request.session.get("propertyNumber").getOrElse("1")).toInt,
                          request.session.get("postcode").getOrElse(""),
                          request.session.get("businessType").getOrElse(""),
                          request.session.get("arn"))
    val emptyForm = AgentForm.form.fill(Agent(""))
    val formWithErrors = AgentForm.form.fill(Agent("")).withGlobalError("NotFound")

    AgentForm.form.bindFromRequest.fold (
      formWithErrors => {
        Future.successful(BadRequest(dashboardPage(clientOne, formWithErrors)))
      },

      success =>{
        dataConnector.addArn(clientOne, success) map {
          case true => Ok(dashboardPage(clientOne.copy(arn = Some(success.arn)), emptyForm)).withSession(request.session + ("arn" -> success.arn))
          case false => BadRequest(dashboardPage(clientOne, formWithErrors))
        }
      }
    )
  }

  def arnRemove: Action[AnyContent] = Action.async { implicit request =>
    val clientOne = Client(request.session.get("crn").getOrElse(""),
                          request.session.get("name").getOrElse(""),
                          request.session.get("businessName").getOrElse(""),
                          request.session.get("contactNumber").getOrElse(""),
                          (request.session.get("propertyNumber").getOrElse("1")).toInt,
                          request.session.get("postcode").getOrElse(""),
                          request.session.get("businessType").getOrElse(""),
                          request.session.get("arn"))
    val emptyForm = AgentForm.form.fill(Agent(""))
    clientOne.arn match {
      case Some(arn) =>
        dataConnector.removeArn(clientOne, Agent(arn)).map {
          case true => Ok(dashboardPage(clientOne.copy(arn = None), emptyForm)).withSession(request.session - ("arn"))
          case false => BadRequest(dashboardPage(clientOne, emptyForm))
        }
      case None => Future(BadRequest(dashboardPage(clientOne, emptyForm)))
    }
  }
}
