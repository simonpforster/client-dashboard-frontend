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

package uk.gov.hmrc.examplefrontend.common

import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, MessagesRequest, Result}
import uk.gov.hmrc.examplefrontend.controllers.routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Utils {

	def loggedInCheckAsync(request: MessagesRequest[AnyContent],
												 func:(String) => Future[Result]): Future[Result] = {
		request.session.get(SessionKeys.crn) match {
			case Some(reg) => func(reg)
			case None => Future(Redirect(routes.HomePageController.homepage()))
		}
	}
//	def loggedInCheck(request: MessagesRequest[AnyContent],
//										func:(String) => Result): Result = {
//		request.session.get(SessionKeys.crn) match {
//			case Some(reg) => func(reg)
//			case None => Redirect(routes.HomePageController.homepage())
//		}
//	}
}
