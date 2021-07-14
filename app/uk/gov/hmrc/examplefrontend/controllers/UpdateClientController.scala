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

import akka.http.scaladsl.model.HttpHeader.ParsingResult
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models.{Client, UserProperty, UserPropertyForm}
import uk.gov.hmrc.examplefrontend.views.html.{UpdateClientPage, UpdateClientPropertyPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateClientController @Inject()(
                                        mcc: MessagesControllerComponents,
                                        updateClientPage: UpdateClientPage,
                                        updateClientPropertyPage: UpdateClientPropertyPage,
                                        dataConnector: DataConnector,
                                        error: ErrorHandler,
                                        implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def OpenUpdateClientPage: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      dataConnector.readOne(request.session.get(SessionKeys.crn).get).map {
        case Some(client) => Ok(updateClientPage(client))
        case _ => BadRequest(error.standardErrorTemplate(
          pageTitle = ErrorMessages.pageTitle,
          heading = ErrorMessages.heading,
          message = ErrorMessages.message))
      }.recover {
        case _ => InternalServerError(error.standardErrorTemplate(
          pageTitle = ErrorMessages.pageTitle,
          heading = ErrorMessages.heading,
          message = ErrorMessages.message))
      }
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }

  def OpenUpdateClientProperty: Action[AnyContent] = Action(implicit request => {
    val form: Form[UserProperty] = request.session.get(SessionKeys.property).fold(
      UserPropertyForm.submitForm.fill(UserProperty("", ""))) { property =>
      UserPropertyForm.submitForm.fill(UserProperty.decode(property))
    }
    Ok(updateClientPropertyPage(form))
  })

  def updateClientPropertySubmit = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      UserPropertyForm.submitForm.bindFromRequest().fold({ formWithErrors =>
        Future.successful(BadRequest(updateClientPropertyPage(formWithErrors)))
      }, { success =>
        dataConnector.readOne(request.session.get(SessionKeys.crn).getOrElse("")).flatMap {
          case Some(client) =>
            val newClient = Client(
              client.crn,
              client.name,
              client.businessName,
              client.contactNumber,
              success.propertyNumber,
              success.postcode,
              client.businessType
            )
            dataConnector.update(newClient).map {
              case true => Redirect(routes.UpdateClientController.OpenUpdateClientPage())
              case false => NotImplemented(error.standardErrorTemplate(
                pageTitle = ErrorMessages.pageTitle,
                heading = ErrorMessages.heading,
                message = ErrorMessages.message))
            }.recover {
              case _ => NotFound(error.standardErrorTemplate(
                pageTitle = ErrorMessages.pageTitle,
                heading = ErrorMessages.heading,
                message = ErrorMessages.message))
            }
          case None => Future.successful(NotFound(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message)))
        }.recover {
          case _ => InternalServerError(error.standardErrorTemplate(
            pageTitle = ErrorMessages.pageTitle,
            heading = ErrorMessages.heading,
            message = ErrorMessages.message))
        }
      })
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))
    }
  }
}
