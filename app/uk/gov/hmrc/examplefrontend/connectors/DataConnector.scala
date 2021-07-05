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

  private val host = "http://localhost:9006"

  private def wspost(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(host + url).addHttpHeaders("Content-Type" -> "application/json").post(jsObject)

  private def wspatch(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(host + url).addHttpHeaders("Content-Type" -> "application/json").patch(jsObject)

  private def wsdelete(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(host + url).withBody(jsObject).delete()

  def login(user: User): Future[Option[Client]] = {
    val userCredentials = Json.obj(
      "crn" -> user.crn,
      "password" -> user.password
    )
    wspost(url = "/login", jsObject = userCredentials).map { response =>
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

  def deleteClient(crn: CRN): Future[Boolean] = {
    val newCRN: JsObject = Json.obj(
      "crn" -> crn.crn
    )
    wsdelete(url = "/delete-client", jsObject = newCRN).map(
      _.status match {
        case 204 => true
        case _ => false
      })
  }

  def addArn(client: Client, agent: Agent): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      "crn" -> client.crn,
      "arn" -> agent.arn
    )
    wspatch(url = "/add-agent", jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }

  def removeArn(client: Client, agent: Agent): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      "crn" -> client.crn,
      "arn" -> agent.arn
    )
    wspatch(url = "/remove-agent", jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }
}
