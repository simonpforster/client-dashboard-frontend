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

object UrlKeys {
  val agents: String = "/agents/"
  val clients: String = "/clients/"
  val host: String = "http://localhost:9006"
  val agentHost: String = "http://localhost:9009"
  val registrationNI: String = "http://localhost:9007/client-registration/name-input"
  //Client - routes
  val dashboard: String = "dashboard"
  val modifyClient:String = "modify-client"
  //Client backend - routes
  def readOneClient(crn:String): String = clients + crn
  //Add & remove agent - routes
  def addAgent(crn: String): String = clients + crn +"/add"
  def removeAgent(crn: String): String = clients + crn +"/remove"
  //Client - routes
  def login(crn: String): String = clients + crn + "/login"
  def deleteClient(crn: String): String = clients + crn
  def readAgent(arn:String):String = agents + arn + "/details"
  //Client update - routes
  def updateClientName(crn: String): String = clients + crn + "/name"
  def updateContactNumber(crn: String):String = clients + crn + "/contact-number"
  def updateProperty(crn: String):String = clients + crn + "/property"
  def updateBusiness(crn: String): String = clients + crn + "/business-type"
}
