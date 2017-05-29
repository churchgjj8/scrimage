/*
   Copyright 2013 Stephen K Samuel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.sksamuel.scrimage.filter

import java.awt.image.BufferedImage

import com.sksamuel.scrimage.{MutableAwtImage, AwtImage, Image, Filter}
import java.awt.Graphics2D
import thirdparty.romainguy.BlendComposite

import scala.concurrent.ExecutionContext

/** @author Stephen Samuel */
class SummerFilter(vignette: Boolean)(implicit executor: ExecutionContext) extends Filter {

  val summer = Image.fromResource("/com/sksamuel/scrimage/filter/summer1.jpg")

  def apply(image: Image) {
    val scaled = Image.wrapAwt(summer.scaleTo(image.width, image.height).awt, BufferedImage.TYPE_INT_ARGB)
    val g2 = image.awt.getGraphics.asInstanceOf[Graphics2D]
    g2.setComposite(BlendComposite.getInstance(BlendComposite.BlendingMode.INVERSE_COLOR_BURN, 0.5f))
    g2.drawImage(scaled.awt, 0, 0, null)
    g2.dispose()
    if (vignette)
      VignetteFilter(0.92f, 0.98f, 0.3).apply(image)
  }
}

object SummerFilter {
  def apply(vignette: Boolean = true)(implicit executor: ExecutionContext): SummerFilter = new SummerFilter(vignette)
}
