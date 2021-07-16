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
import uk.gov.hmrc.examplefrontend.common.{ErrorMessages, UserClientProperties}

case class User(crn: String,
                password: String)

object UserForm {
  val form: Form[User] =
    Form(
      mapping(
        UserClientProperties.crn -> nonEmptyText,
        UserClientProperties.password -> nonEmptyText
      )(User.apply)(User.unapply)
    )
}

case class UserName(name: String)

object UserNameForm {
  val submitForm: Form[UserName] =
    Form(
      mapping(
        UserClientProperties.name -> Forms.text.verifying(ErrorMessages.nameFormError, _.nonEmpty)
      )(UserName.apply)(UserName.unapply)
    )
}


case class UserContactNumber(contact: String)

object UserContactNumberForm {
  val submitForm: Form[UserContactNumber] =
    Form(
      mapping(
        UserClientProperties.contactNumber -> Forms.text.verifying(ErrorMessages.contactNumberFormError, _.length >= 10)
      )(UserContactNumber.apply)(UserContactNumber.unapply)
    )
}


