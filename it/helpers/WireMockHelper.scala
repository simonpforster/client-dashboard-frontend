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

package helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait WireMockHelper {

  val wiremockPort = 9006
  val wiremockHost = "localhost"
  lazy val wmConfig: WireMockConfiguration = wireMockConfig().port(wiremockPort)
  lazy val wireMockServer: WireMockServer = new WireMockServer(wmConfig)
  val headerKey: String = "Content-Type"
  val headerValues: String = "text/json"

  def startWiremock(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wiremockHost, wiremockPort)
  }

  def stopWiremock(): Unit = wireMockServer.stop()

  def resetWiremock(): Unit = WireMock.reset()


  def stubPost(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(post(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )

  def stubPut(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(put(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )

  def stubGet(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )

  def stubGet(url: String, status: Integer): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withHeader(headerKey, headerValues)
      )
    )

  def stubPatch(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(patch(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )

  def stubDelete(url: String, status: Integer, responseBody: String): StubMapping =
    stubFor(delete(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(responseBody)
      )
    )
}