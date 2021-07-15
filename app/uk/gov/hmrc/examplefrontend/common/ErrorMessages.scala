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

object ErrorMessages {
  val pageTitle: String = "Something went wrong"
  val heading: String = "Something went wrong"
  val message: String = "Please come back later"
  val pageTitleUpdateContactNumber: String = "Something went wrong"
  val headingUpdateContactNumber: String = "Something went wrong, your contact number couldn't be updated"
  val messageUpdateContactNumber: String = "Please come back later"
  val nameFormError: String = "Name must not be empty"
  val businessNameFormError: String = "Business name must not be empty"
  val contactNumberFormError: String = "Phone number must be 10 or more digits"
  val propertyNumberFormError: String = "Property number must not be empty"
  val postcodeFormError: String = "Postcode must not be empty"
  val businessTypeFormError: String = "Please select one field"
  val passwordFormError: String = "Password field must not be empty"
  val passwordCheckFormError: String = "Password Match field must not be empty"
  val passwordMatch: String = "Passwords do not match"
}
