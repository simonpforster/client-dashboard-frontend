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
}
