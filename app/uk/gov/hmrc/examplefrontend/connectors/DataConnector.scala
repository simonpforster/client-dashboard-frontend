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

package uk.gov.hmrc.examplefrontend.connectors

import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.examplefrontend.models.{Agent, CRN, Client, User}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataConnector @Inject()(ws: WSClient, implicit val ec: ExecutionContext) {

  private val host: String = "http://localhost:9006"
  private def wspost(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(host + url).addHttpHeaders("Content-Type" -> "application/json").post(jsObject)

  def login(user: User): Future[Option[Client]] = {
    val userCredentials = Json.obj(
      "crn" -> user.crn,
      "password" -> user.password
    )
    wspost("/login", userCredentials).map { response =>
      response.status match {
        case 200 =>
          response.json.validate[Client] match {
            case JsSuccess(client, _) => Some(client)
            case JsError(_) => None
          }
        case _ => None
      }
    }
  }

  def wsdelete(url: String, jsObject: JsObject): Future[WSResponse] = {
    ws.url(host + url).withBody(jsObject).delete()
  }

  def deleteClient(crn: CRN): Future[Boolean] = {
    val newCRN: JsObject = Json.obj(
      "crn" -> crn.crn
    )
    wsdelete("/delete-client", newCRN).map(
      _.status match {
        case 204 => true
        case _ => false
      })
  }

  def createObjAndPOST(agent: Agent): Future[Option[Agent]] = {
    val ARN: JsObject = Json.obj(
      "arn" -> agent.arn
    )
    wspost("http://localhost:9005/submit-arn", ARN).map { result => Json.fromJson[Agent](result.json).asOpt }
  }
}
