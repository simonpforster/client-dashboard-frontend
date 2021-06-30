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

package uk.gov.hmrc.examplefrontend.connector

import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import play.api.libs.json.{JsObject, Json}
import play.api.i18n.I18nSupport
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{BaseController, ControllerComponents}
import uk.gov.hmrc.examplefrontend.models.{CRN, Client}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class LoginConnector @Inject()(ws: WSClient, val controllerComponents: ControllerComponents, val ec:ExecutionContext) extends BaseController  with I18nSupport  {

  val backend: String = "http://localhost:9006"
  val registerFrontEnd:String = "http://localhost:9007"

  def wsdelete(url:String,jsObject: JsObject):Future[WSResponse] = {
    ws.url((backend+url)).withBody(jsObject).delete()
  }

  def deleteClient(crn:CRN):Future[Boolean] ={
    val newcrn:JsObject = Json.obj(
      "crn" -> crn.crn
    )

    wsdelete("/deleteClient",newcrn).map(
      _.status match{
        case 204 => true
        case _ => false
      })
  }

}
