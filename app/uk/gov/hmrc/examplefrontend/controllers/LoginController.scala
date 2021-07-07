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
                                 implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def login: Action[AnyContent] = Action { implicit request =>
    if(request.session.get("crn").isDefined){
      Redirect(routes.DashboardController.dashboardMain())
    }else{
      val form: Form[User] = UserForm.form.fill(User(crn = "", password = ""))
      Ok(loginPage(form))
    }
  }

  def logOut: Action[AnyContent] = Action { implicit request =>
    if(request.session.get("crn").isDefined){
      Ok(logoutSuccessPage()).withNewSession
    }else {
      Redirect(routes.HomePageController.homepage())
    }

  }

  def loginSubmit: Action[AnyContent] = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(loginPage(formWithErrors)))
      }, success => {
        dataConnector.login(success).map {
          case Some(client) =>
            val call = Redirect("/example-frontend/dashboard").withSession(request.session
              + ("crn" -> s"${client.crn}")
              + ("name" -> s"${client.name}")
              + ("businessName" -> s"${client.businessName}")
              + ("contactNumber" -> s"${client.contactNumber}")
              + ("propertyNumber" -> s"${client.propertyNumber}")
              + ("postcode" -> s"${client.postcode}")
              + ("businessType" -> s"${client.businessType}")
            )
            client.arn match {
              case Some(arn) => call.withSession(call.session + ("clientArn" -> arn))
              case None => call
            }

          case None => Unauthorized(loginPage(UserForm.form.fill(User("", ""))))
        }
      } recover { case _ => InternalServerError }
    )
  }
}
