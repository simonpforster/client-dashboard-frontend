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

import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.examplefrontend.common.{UrlKeys, UserClientProperties}
import uk.gov.hmrc.examplefrontend.models.{Agent, CRN, Client, User}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataConnector @Inject()(ws: WSClient, implicit val ec: ExecutionContext) {

  val hdrsType: String = "Content-Type"
  val hdrsValue: String = "application/json"

  private def wspostagent(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.agentHost + url)
    .addHttpHeaders(hdrsType -> hdrsValue).post(jsObject)

  private def wspost(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .addHttpHeaders(hdrsType -> hdrsValue).post(jsObject)

  private def wsput(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .addHttpHeaders(hdrsType -> hdrsValue).put(jsObject)

  private def wspatch(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .addHttpHeaders(hdrsType -> hdrsValue).patch(jsObject)

  private def wsdelete(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .withBody(jsObject).delete()
  private def wsget(url:String, jsObject: JsObject):Future[WSResponse] = ws.url(UrlKeys.host + url)
    .withBody(jsObject).get()


  def readOne(crn:String):Future[Option[Client]]={
    val crnObj = Json.obj(
      UserClientProperties.crn -> crn)
    wsget(UrlKeys.readOneClient,crnObj).map{response =>
      response.status match {
        case 200 => response.json.validate[Client] match {
          case JsSuccess(client, _) => Some(client)
          case JsError(_) => None
        }
        case 404 => throw new Exception("NotFound")
      }
    }
  }
  def update(client: Client):Future[Boolean]={
    ws.url(UrlKeys.host + UrlKeys.updateClient).put(Json.toJson(client)).map(
      _.status match {
        case 201 => true
        case 400 => false
      })
  }

  private def wsget(url:String, jsObject: JsObject):Future[WSResponse] = ws.url(UrlKeys.host + url)
    .withBody(jsObject).get()

  def readOne(crn:String):Future[Option[Client]]={
    val crnObj = Json.obj(
      UserClientProperties.crn -> crn)
      wsget(UrlKeys.readOneClient,crnObj).map{response =>
        response.status match {
          case 200 => response.json.validate[Client] match {
            case JsSuccess(client, _) => Some(client)
            case JsError(_) => None
          }
          case 404 => throw new Exception("NotFound")
        }
    }
  }

  def update(client: Client):Future[Boolean]={
    ws.url(UrlKeys.host + UrlKeys.updateClient).put(Json.toJson(client)).map(
      _.status match {
        case 201 => true
        case 400 => false
      })
  }

  def login(user: User): Future[Option[Client]] = {
    val userCredentials: JsObject = Json.obj(
      UserClientProperties.crn -> user.crn,
      UserClientProperties.password -> user.password
    )
    wspost(url = UrlKeys.login, jsObject = userCredentials).map { response =>
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
      UserClientProperties.crn -> crn.crn
    )
    wsdelete(url = UrlKeys.deleteClient, jsObject = newCRN).map(
      _.status match {
        case 204 => true
        case _ => false
      })
  }

  def checkArn(agent: Agent): Future[Boolean] = {
    wspostagent(UrlKeys.readAgent, Json.toJson(agent).as[JsObject]).map {
      _.status match {
        case 200 => true
        case _ => false
      }
    }
  }

  def addArn(client: Client, agent: Agent): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      UserClientProperties.crn -> client.crn,
      UserClientProperties.arn -> agent.arn
    )
    wspatch(url = UrlKeys.addAgent, jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }

  def removeArn(client: Client, agent: Agent): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      UserClientProperties.crn -> client.crn,
      UserClientProperties.arn -> agent.arn
    )
    wspatch(url = UrlKeys.removeAgent, jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }

  def updateContactNumber(crn: String, updatedContactNumber: String): Future[Boolean] = {
    val objectToBeSend = Json.obj(
      UserClientProperties.crn -> crn,
      UserClientProperties.contactNumber -> updatedContactNumber,
    )

    wsput(UrlKeys.updateContactNumber, objectToBeSend).map {
      _.status match {
        case NO_CONTENT => true
        case _ => false
      }
    }
  }


}
