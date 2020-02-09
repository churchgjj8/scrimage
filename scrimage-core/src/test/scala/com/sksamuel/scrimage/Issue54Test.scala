package com.sksamuel.scrimage

import org.scalatest.{FunSuite, Matchers}

/** @author Stephen Samuel */
class Issue54Test extends FunSuite with Matchers {

  test("image for issue 54 should load as expected") {
    Image.fromStream(getClass.getResourceAsStream("/issue54.jpg")).width shouldBe 2560
    Image.fromStream(getClass.getResourceAsStream("/issue54.jpg")).scaleToWidth(400) shouldBe
      Image.fromStream(getClass.getResourceAsStream("/issue54.png"))
  }
}
