package uk.gov.hmrc.examplefrontend.controllers

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

trait AbstractTest extends AnyWordSpec with BeforeAndAfter with Matchers with GuiceOneAppPerSuite {

}
