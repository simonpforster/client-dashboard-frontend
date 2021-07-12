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
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.examplefrontend.common.SessionKeys
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.models.{Client, UserProperty, UserPropertyForm}
import uk.gov.hmrc.examplefrontend.views.html.{UpdateClientPage, UpdateClientPropertyPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateClientController @Inject()(
                                        mcc: MessagesControllerComponents,
                                        updateClientPage: UpdateClientPage,
                                        updateClientPropertyPage:UpdateClientPropertyPage,
                                        error: ErrorHandler,
                                        implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def OpenUpdateClientPage: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val crn: String = request.session.get(SessionKeys.crn).getOrElse("")
      val name = request.session.get(SessionKeys.name).getOrElse("")
      val businessName = request.session.get(SessionKeys.businessName).getOrElse("")
      val contactNumber: String = request.session.get(SessionKeys.contactNumber).getOrElse("")
      val propertyNumber: String = request.session.get(SessionKeys.propertyNumber).getOrElse("")
      val postcode:String = request.session.get(SessionKeys.postcode).getOrElse("")
      val businessType: String = request.session.get(SessionKeys.businessType).getOrElse("")
      val arn: Option[String] = request.session.get(SessionKeys.arn)
      val clientOne = Client(crn, name, businessName, contactNumber, propertyNumber,postcode, businessType, arn)
      Future.successful(Ok(updateClientPage(clientOne)))
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }

  def OpenUpdateClientProperty = Action async  {implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {

      val form: Form[UserProperty] = request.session.get(SessionKeys.property).fold(UserPropertyForm.submitForm.fill(UserProperty("", ""))) {
        property => UserPropertyForm.submitForm.fill(UserProperty.decode(property))
      }

      val crn: String = request.session.get(SessionKeys.crn).getOrElse("")
      val name = request.session.get(SessionKeys.name).getOrElse("")
      val businessName = request.session.get(SessionKeys.businessName).getOrElse("")
      val contactNumber: String = request.session.get(SessionKeys.contactNumber).getOrElse("")
      val property: UserProperty = UserProperty.decode(request.session.get(SessionKeys.property).getOrElse(""))
      val businessType: String = request.session.get(SessionKeys.businessType).getOrElse("")
      val arn: Option[String] = request.session.get(SessionKeys.arn)

      val clientOne = Client(crn, name, businessName, contactNumber, property.propertyNumber,property.postcode, businessType, arn)
      Future.successful(Ok(updateClientPropertyPage(form,clientOne)))
    }else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }

  def updateProperty = Action async {implicit request =>
    Future.successful(Ok)
  }

}
