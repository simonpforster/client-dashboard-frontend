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

  val clients: String = "/clients/"
  val host: String = "http://localhost:9006"
  val agentHost: String = "http://localhost:9009"
  val registrationNI: String = "http://localhost:9007/client-registration/name-input"


  //Client - routes
  val client = "/client/"

  val registration: String = "registration"
  val login: String = "login"
  val logOut: String = "successful-logout"
  val dashboard: String = "dashboard"
  val arnRemove: String = "arn-remove"
  val arnSubmit: String = "arn-remove"
  val deleteClient: String = "delete-user"
  val areYouSure: String = "delete-select"
  val deleteClientSuccessful: String = "deleted-client"
  val updateClientPage: String = "update-client"
  val updateClientName: String = "update-name"
  val updateContactNumber: String = "update-contact-number"
  val updateProperty: String = "update-property"
  val updateBusiness: String = "update-business-type"

  //Clients backend - routes
  def readOneClient(crn: String): String = clients + crn

  def addAgent(crn: String): String = clients + crn + "/add"

  def removeAgent(crn: String): String = clients + crn + "/remove"

  def login(crn: String): String = clients + crn + "/login"

  def deleteClient(crn: String): String = clients + crn

  def updateClientName(crn: String): String = clients + crn + "/name"

  def updateContactNumber(crn: String): String = clients + crn + "/contact-number"

  def updateProperty(crn: String): String = clients + crn + "/property"

  def updateBusiness(crn: String): String = clients + crn + "/business-type"

  //Agents backend - routes
  val agents: String = "/agents/"

  def readAgent(arn: String): String = agents + arn + "/details"
}
