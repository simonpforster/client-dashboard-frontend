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


import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.examplefrontend.models.Agent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ARNConnector @Inject()(ws: WSClient, implicit val ec: ExecutionContext)  {

  def wspost(url: String, objToBeSent: JsObject): Future[WSResponse] = {
    ws.url(url).post(objToBeSent)
  }

  def createObjAndPOST(agent: Agent): Future[Option[Agent]] = {
    val ARN = Json.obj(
      "arn" -> agent.arn
    )

    wspost("http://localhost:9005/submit-arn", ARN).map{ result => Json.fromJson[Agent](result.json).asOpt}
  }
}
