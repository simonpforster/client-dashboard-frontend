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
  val host: String = "http://localhost:9006"
  val agentHost: String = "http://localhost:9009"
  val registrationNI: String = "http://localhost:9007/client-registration/name-input"
  val frontend: String = "/example-frontend"
  val dashboard: String = "/client/dashboard"
  val clientRegistration: String = "/client/registration"
  val deleteClient: String = "/delete-client"
  val deleteSelect: String = "/example-frontend/delete-select"
  val addAgent: String = "/add-agent"
  val readAgent: String = "/readAgent"
  val removeAgent: String = "/remove-agent"
  val arnSumbit: String = "/arn-submit"
  val login: String = "/login"
  val clientLogin: String = "/client/login"
  val modifyClient:String ="/modify-client"
  val updateContactNumber:String ="/update-contact-number"
  val updateClient:String = "/update-client"
  val readOneClient:String = "/read"
  val updateProperty:String = "/update-property"
  val modifyClientProperty:String = "/modify-client-property"

  val updateBusiness: String = "/update-business"
  val updateClientName: String = "/update-name"
}
