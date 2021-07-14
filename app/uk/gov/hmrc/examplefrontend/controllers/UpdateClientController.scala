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
import play.api.http.Writeable.wByteArray
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, SessionKeys, Utils}
import uk.gov.hmrc.examplefrontend.config.ErrorHandler
import uk.gov.hmrc.examplefrontend.connectors.DataConnector
import uk.gov.hmrc.examplefrontend.models._
import uk.gov.hmrc.examplefrontend.views.html.{UpdateBusinessTypePage, UpdateClientPage, UpdateClientPropertyPage, UpdateContactNumber, UpdateNamePage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateClientController @Inject()(
                                        mcc: MessagesControllerComponents,
                                        updateClientPage: UpdateClientPage,
                                        nameUpdatePage: UpdateNamePage,
                                        updateClientPropertyPage: UpdateClientPropertyPage,
                                        updateContactNumberPage: UpdateContactNumber,
                                        updateBusinessTypePage: UpdateBusinessTypePage,
                                        dataConnector: DataConnector,
                                        error: ErrorHandler,
                                        implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def openUpdateClientPage: Action[AnyContent] = Action async { implicit request =>
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

  def openUpdateClientProperty: Action[AnyContent] = Action(implicit request => {
    val form: Form[UserProperty] = request.session.get(SessionKeys.property).fold(
      UserPropertyForm.submitForm.fill(UserProperty("", ""))) { property =>
      UserPropertyForm.submitForm.fill(UserProperty.decode(property))
    }
    val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
    Ok(updateClientPropertyPage(form,registeredClient ))
  })

  def updateClientPropertySubmit: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
      UserPropertyForm.submitForm.bindFromRequest().fold({ formWithErrors =>
        Future.successful(BadRequest(updateClientPropertyPage(formWithErrors, registeredClient)))
      }, { success =>
        dataConnector.updateProperyDetails(success.propertyNumber, success.postcode, request.session.get(SessionKeys.crn).get).map {
          case true => Redirect(routes.UpdateClientController.openUpdateClientPage())
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
      })
    } else {
      Future.successful(Redirect(routes.HomePageController.homepage()))

    }
  }

  def updateName: Action[AnyContent] = Action { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      Ok(nameUpdatePage(UserNameForm.submitForm.fill(UserName(""))))
    } else {
      Redirect(routes.HomePageController.homepage())
    }
  }

  def updateNameSubmit: Action[AnyContent] = Action.async { implicit request =>
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

  def updateContactNumber: Action[AnyContent] = Action{ implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val form = UserContactNumberForm.submitForm.fill(UserContactNumber(""))
      val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
      Ok(updateContactNumberPage(form, registeredClient))
    } else Redirect(routes.LoginController.login())
  }

  def submitUpdatedContactNumber: Action[AnyContent] = Action async { implicit request =>
    if (request.session.get(SessionKeys.crn).isDefined) {
      val registeredClient = Client(request.session.get(SessionKeys.crn).get, "", "", "", "", "", "")
      UserContactNumberForm.submitForm.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(updateContactNumberPage(formWithErrors, registeredClient))),
        success => {
          dataConnector.updateContactNumber(request.session.get(SessionKeys.crn).get, success.contact).map {
            case true => Redirect(routes.UpdateClientController.openUpdateClientPage())
            case false => NotImplemented(error.standardErrorTemplate(
              pageTitle = ErrorMessages.pageTitleUpdateContactNumber,
              heading = ErrorMessages.headingUpdateContactNumber,
              message = ErrorMessages.messageUpdateContactNumber
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

  def updateBusinessType: Action[AnyContent] = Action { implicit request =>
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