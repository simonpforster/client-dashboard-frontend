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

import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.examplefrontend.views.html.DashboardPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.models.{Agent, AgentForm, Client}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject()(
                                     ws: WSClient,
                                     mcc: MessagesControllerComponents,
                                     dashboardPage: DashboardPage,
                                     implicit val ec: ExecutionContext
                                      )
  extends FrontendController(mcc){

  def dashboardMain: Action[AnyContent] = Action.async { implicit request =>
    val clientOne = Client(request.session.get("crn").getOrElse(""), request.session.get("name").getOrElse(""), "","", 0, "", "" )
    Future.successful(Ok(dashboardPage(clientOne, AgentForm.form.fill(Agent("")), "")))
  }

  def clientName: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.DashboardController.dashboardMain())
      .withSession(request.session + ("name" -> "John Doe" ) + ("crn" -> "asd39402" ))
  }

  def arnSubmit = Action.async { implicit request =>
    val clientOne = Client(request.session.get("crn").getOrElse(""), request.session.get("name").getOrElse(""), "","", 0, "", "" )
    AgentForm.form.bindFromRequest.fold (
      formWithErrors => {
        Future.successful(BadRequest(dashboardPage(clientOne, formWithErrors, "")))
      },

      success => {
        val ARN = Json.obj(
          "arn" -> success.arn
        )
        val futureResponse = ws.url("http://localhost:9005/submit-arn").post(ARN)

        futureResponse.map{
          response =>{
            val jsObject = Json.fromJson[Agent](response.json)
            val agent = jsObject.get

            response.status match {
              case 200 => Ok(dashboardPage(clientOne, AgentForm.form.fill(Agent("")), agent.arn))
              case _ => BadRequest(dashboardPage(clientOne, AgentForm.form.withGlobalError("NotFound"), agent.arn))
            }
          }
        } recover {
          case error => InternalServerError
        }

      }
    )
  }

}
