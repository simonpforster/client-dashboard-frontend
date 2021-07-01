package intergrationTest

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, urlMatching}

class DeleteClientIntergration extends WireMockHelper {

  "The User" should{
    "send delete request" in{
      wireMockServer.stubFor(delete(urlMatching( s"/deleteClient")).willReturn(aResponse().withBody("yes").withStatus(200)))
      val result:Boolean = await(connector.deleteClient(crn))
      result should be (true)
    }
  }

}
