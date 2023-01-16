package com.sksamuel.scrimage.nio;

import com.sksamuel.scrimage.AwtImage;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;

import javax.imageio.ImageWriter;
import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Baseed on work by Elliot Kroo on 2009-04-25 and adapted into Java and rewritten.
 * <p>
 * This work is licensed under the Creative Commons Attribution 3.0 Unported
 * License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
 * Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 * <p>
 * Typical usage will look something like:
 * {{{
 * import com.sksamuel.scrimage.nio.StreamingGifWriter
 * val stream = StreamingGifWriter()
 * stream.prepareStream("/path/to/gif.gif", BufferedImage.TYPE_INT_ARGB)
 * stream.writeFrame(image0)
 * stream.writeFrame(image1)
 * stream.writeFrame(imageN)
 * stream.finish()
 * }}}
 */
public class StreamingGifWriter extends AbstractGifWriter {

   private final Duration frameDelay;
   private final boolean infiniteLoop;
   private final boolean compressed;

   public StreamingGifWriter() {
      this.frameDelay = Duration.ofSeconds(2);
      this.infiniteLoop = true;
      this.compressed = false;
   }

   public StreamingGifWriter(Duration frameDelay, boolean infiniteLoop, boolean compressed) {
      this.frameDelay = frameDelay;
      this.infiniteLoop = infiniteLoop;
      this.compressed = compressed;
   }

   public StreamingGifWriter withFrameDelay(Duration delay) {
      return new StreamingGifWriter(delay, infiniteLoop, compressed);
   }

   public StreamingGifWriter withInfiniteLoop(boolean infiniteLoop) {
      return new StreamingGifWriter(frameDelay, infiniteLoop, compressed);
   }

   public StreamingGifWriter withCompression(boolean compressed) {
      return new StreamingGifWriter(frameDelay, infiniteLoop, compressed);
   }

   public interface GifStream extends AutoCloseable {
      GifStream writeFrame(ImmutableImage image) throws IOException;

      GifStream writeFrame(ImmutableImage image, Duration delay) throws IOException;

      GifStream writeFrame(ImmutableImage image, DisposeMethod disposeMethod) throws IOException;

      GifStream writeFrame(ImmutableImage image, Duration delay, DisposeMethod disposalMethod) throws IOException;
   }

   public GifStream prepareStream(String path, int imageType) throws IOException {
      return prepareStream(Paths.get(path), imageType);
   }

   public GifStream prepareStream(Path path, int imageType) throws IOException {
      return prepareStream(path.toFile(), imageType);
   }

   public GifStream prepareStream(File file, int imageType) throws IOException {
      FileOutputStream output = new FileOutputStream(file);
      return prepareStream(output, imageType);
   }

   public GifStream prepareStream(OutputStream output, int imageType) throws IOException {

      ImageWriter writer = ImageIO.getImageWritersBySuffix("gif").next();
      ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();

      ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
      IIOMetadata imageMetaData = writer.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

      String metaFormatName = imageMetaData.getNativeMetadataFormatName();

      IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
      populateGraphicsControlNode(root, frameDelay);
      populateCommentsNode(root);
      populateApplicationExtensions(root, infiniteLoop);

      imageMetaData.setFromTree(metaFormatName, root);

      MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(output);

      writer.setOutput(ios);
      writer.prepareWriteSequence(null);

      return new GifStream() {

         private ImmutableImage last = null;

         @Override
         public GifStream writeFrame(ImmutableImage image) throws IOException {
            if (compressed && last != null) {
               DataBuffer inputBuffer = image.awt().getRaster().getDataBuffer();
               DataBuffer lastBuffer = last.awt().getRaster().getDataBuffer();
               // must copy the image before assigning it to last, because we will modify it in the next step
               last = image.copy();
               for (int i = 0; i < inputBuffer.getSize(); i++) {
                  if (inputBuffer.getElem(i) == lastBuffer.getElem(i)) {
                     inputBuffer.setElem(i, 0);
                  }
               }
               writer.writeToSequence(new IIOImage(image.awt(), null, imageMetaData), imageWriteParam);
            } else {
               writer.writeToSequence(new IIOImage(image.awt(), null, imageMetaData), imageWriteParam);
               last = image;
            }
            return this;
         }

         @Override
         public GifStream writeFrame(ImmutableImage image, Duration delay) throws IOException {
            return writeFrame(image, delay, null);
         }

         @Override
         public GifStream writeFrame(ImmutableImage image, DisposeMethod disposeMethod) throws IOException {
            return writeFrame(image, null, disposeMethod);
         }

         @Override
         public GifStream writeFrame(ImmutableImage image, Duration delay, DisposeMethod disposalMethod) throws IOException {
            setOverriddenGraphicControlExtensionAttributes(delay, disposalMethod);
            writer.writeToSequence(new IIOImage(image.awt(), null, imageMetaData), imageWriteParam);
            return this;
         }

         private void setOverriddenGraphicControlExtensionAttributes(Duration delay, DisposeMethod disposeMethod)
            throws IIOInvalidTreeException {
            IIOMetadataNode rootOverride = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
            IIOMetadataNode graphicsControlExtensionNodeOverride = getNode(rootOverride, "GraphicControlExtension");
            if (delay != null) {
               graphicsControlExtensionNodeOverride.setAttribute("delayTime", (delay.toMillis() / 10L) + "");
            }
            if (disposeMethod != null) {
               graphicsControlExtensionNodeOverride.setAttribute("disposalMethod", disposeMethod.getDisposeMethodValue());
            }
            imageMetaData.setFromTree(metaFormatName, rootOverride);
         }

         @Override
         public void close() throws IOException {
            writer.endWriteSequence();
            writer.dispose();
            ios.close();
            output.close();
         }
      };
   }

}
