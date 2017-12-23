package com.sksamuel.scrimage.composite

import com.sksamuel.scrimage.Image
import org.scalatest.{BeforeAndAfter, FunSuite, OneInstancePerTest}

class AlphaCompositeTest extends FunSuite with BeforeAndAfter with OneInstancePerTest {

  private val source = Image.fromResource("/colosseum.jpg").resizeTo(400, 300)
  private val transparent = Image.fromResource("/transparent_chip.png")
  private val expected1 = Image.fromResource("/composite/alpha_composite.png")
  private val expected2 = Image.fromResource("/composite/alpha_composite_0.5f.png")

  test("alpha composite uses transparency of application image") {
    val actual = source.composite(new AlphaComposite(1f), transparent)
    assert(expected1 === actual)
  }

  test("alpha composite uses transparency of application image combined with alpha") {
    val actual = source.composite(new AlphaComposite(0.5f), transparent)
    assert(expected2 === actual)
  }
}
