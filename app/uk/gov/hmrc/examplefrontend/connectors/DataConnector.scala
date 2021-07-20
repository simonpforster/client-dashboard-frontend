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
import uk.gov.hmrc.examplefrontend.models.{Agent, Client, User}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataConnector @Inject()(ws: WSClient, implicit val ec: ExecutionContext) {
  val hdrsType: String = "Content-Type"
  val hdrsValue: String = "application/json"

  private def wsgetagent(url: String): Future[WSResponse] = ws.url(UrlKeys.agentHost + url)
    .addHttpHeaders(hdrsType -> hdrsValue).get()

  private def wspost(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .addHttpHeaders(hdrsType -> hdrsValue).post(jsObject)

  private def wspatch(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .addHttpHeaders(hdrsType -> hdrsValue).patch(jsObject)

  private def wsdelete(url: String, jsObject: JsObject): Future[WSResponse] = ws.url(UrlKeys.host + url)
    .withBody(jsObject).delete()

  private def wsget(url: String): Future[WSResponse] = ws.url(UrlKeys.host + url).get()

  def readOne(crn: String): Future[Option[Client]] = {
    wsget(UrlKeys.readOneClient(crn)).map { response =>
      response.status match {
        case 200 => response.json.validate[Client] match {
          case JsSuccess(client, _) => Some(client)
          case JsError(_) => None
        }
        case 404 => throw new Exception("NotFound")
      }
    }
  }

  def updateBusinessType(crn: String, businessType: String): Future[Boolean] = {
    val objToSend = Json.obj(
      UserClientProperties.crn -> crn,
      UserClientProperties.businessType -> businessType
    )
    ws.url(UrlKeys.host + UrlKeys.updateBusiness(crn)).patch(Json.toJson(objToSend)).map {
      _.status match {
        case NO_CONTENT => true
        case _ => false
      }
    }
  }

  def update(client: Client): Future[Boolean] = {
    ws.url(UrlKeys.host + UrlKeys.updateClientName(client.crn)).put(Json.toJson(client)).map(
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
    wspost(url = UrlKeys.login(user.crn), jsObject = userCredentials).map { response =>
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

  def deleteClient(crn: String): Future[Boolean] = {
    val newCRN: JsObject = Json.obj(
      UserClientProperties.crn -> crn
    )
    wsdelete(url = UrlKeys.deleteClient(crn), jsObject = newCRN).map(
      _.status match {
        case 204 => true
        case _ => false
      })
  }

  def checkArn(agent: Agent): Future[Boolean] = {
    wsgetagent(UrlKeys.readAgent(agent.arn)).map {
      _.status match {
        case 200 => true
        case _ => false
      }
    }
  }

  def addArn(crn: String, arn: String): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      UserClientProperties.arn -> arn
    )
    wspatch(url = UrlKeys.addAgent(crn), jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }

  def removeArn(crn:String, arn:String): Future[Boolean] = {
    val clientAgentPair = Json.obj(
      UserClientProperties.arn -> arn
    )
    wspatch(url = UrlKeys.removeAgent(crn), jsObject = clientAgentPair).map {
      _.status match {
        case 204 => true
        case _ => false
      }
    }
  }

  def updateClientName(crn: String, newName: String): Future[Boolean] = {
    val jsObj = Json.obj(
      UserClientProperties.crn -> crn,
      UserClientProperties.name -> newName
    )

    wspatch(UrlKeys.updateClientName(crn), jsObj).map {
      _.status match {
        case NO_CONTENT => true
        case _ => false
      }
    }
  }

  def updateContactNumber(crn: String, updatedContactNumber: String): Future[Boolean] = {
    val objectToBeSend = Json.obj(
      UserClientProperties.crn -> crn,
      UserClientProperties.contactNumber -> updatedContactNumber,
    )

    wspatch(UrlKeys.updateContactNumber(crn), objectToBeSend).map {
      _.status match {
        case NO_CONTENT => true
        case _ => false
      }
    }
  }

  def updateProperyDetails(propertyNumber: String, postcode: String, crn: String): Future[Boolean] = {
    val property = Json.obj(
      UserClientProperties.crn -> crn,
      UserClientProperties.propertyNumber -> propertyNumber,
      UserClientProperties.postcode -> postcode,
    )
    ws.url(UrlKeys.host + UrlKeys.updateProperty(crn)).patch(Json.toJson(property)).map(
      _.status match {
        case NO_CONTENT => true
        case _ => false
      }
    )
  }
}