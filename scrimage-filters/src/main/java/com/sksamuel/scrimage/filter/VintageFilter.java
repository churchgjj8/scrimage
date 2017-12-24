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
package com.sksamuel.scrimage.filter;

import com.sksamuel.scrimage.Filter;
import com.sksamuel.scrimage.Image;
import thirdparty.misc.ThistleFilter;

import java.awt.*;
import java.awt.image.BufferedImage;

public class VintageFilter implements Filter {

    @Override
    public void apply(Image image) {
        ThistleFilter thistle = new ThistleFilter();
        BufferedImage filtered = thistle.filter(image.awt());
        Graphics2D g2 = (Graphics2D) image.awt().getGraphics();
        g2.drawImage(filtered, 0, 0, null);
        g2.dispose();
    }
}
