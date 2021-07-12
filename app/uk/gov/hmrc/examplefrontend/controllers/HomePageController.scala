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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.SessionKeys
import uk.gov.hmrc.examplefrontend.views.html.HomePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class HomePageController @Inject()(mcc: MessagesControllerComponents,
                                   homePage: HomePage)
  extends FrontendController(mcc) {

  def homepage: Action[AnyContent] = Action { implicit request =>
    if(request.session.get(SessionKeys.crn).isDefined){
      Redirect(routes.DashboardController.dashboardMain())
    }else{
      Ok(homePage())
    }
  }

  def registration: Action[AnyContent] = Action {
    Redirect("http://localhost:9007/client-registration/name-input").withNewSession
  }
}

