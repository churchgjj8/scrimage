package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.Image
import org.scalatest.{BeforeAndAfter, FunSuite, OneInstancePerTest}

class OilFilterTest extends FunSuite with BeforeAndAfter with OneInstancePerTest {

  private val original = getClass.getResourceAsStream("/bird_small.png")
  private val expected = getClass.getResourceAsStream("/com/sksamuel/scrimage/filters/bird_small_oil.png")

  test("filter output matches expected") {
    assert(Image(original).filter(new OilFilter()) === Image(expected))
  }
}
