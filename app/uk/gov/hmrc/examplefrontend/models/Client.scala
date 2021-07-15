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

package uk.gov.hmrc.examplefrontend.models


import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, UserClientProperties}

case class Client(crn: String,
                  name: String,
                  businessName: String,
                  contactNumber: String,
                  propertyNumber: String,
                  postcode: String,
                  businessType: String,
                  arn: Option[String] = None)

object Client {
  implicit val format: OFormat[Client] = Json.format[Client]
}

case class UserProperty(propertyNumber: String, postcode: String) {
  def encode(): String = {
    propertyNumber + "/" + postcode
  }
}

object UserProperty {
  def decode(x: String): UserProperty = {
    val (propertyNumber, postcode): (String, String) = x.split("/").toList match {
      case propertyNumber :: postcode :: _ => (propertyNumber, postcode)
    }
    UserProperty(propertyNumber = propertyNumber, postcode = postcode)
  }
}

object UserPropertyForm {
  val submitForm: Form[UserProperty] =
    Form(
      mapping(
        UserClientProperties.propertyNumber -> Forms.text.verifying(ErrorMessages.propertyNumberFormError, _.nonEmpty),
        UserClientProperties.postcode -> Forms.text.verifying(ErrorMessages.postcodeFormError, _.isEmpty == false)
      )(UserProperty.apply)(UserProperty.unapply)
    )
}
