package at.javaprofi.ocr.frame.backend.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.Stream;

import net.sourceforge.tess4j.util.ImageHelper;

/**
 * custom implementation of FrameConsumer
 * used by FFMpeg instance to scale and write its consumed frames to *.jpeg files
 */
public class VideoFrameConsumer implements FrameConsumer
{

    private final Path pathToDstDir;
    private final AtomicLong duration;
    private final Rectangle boundingBox;

    public VideoFrameConsumer(Path pathToDstDir, AtomicLong duration, Rectangle boundingBox)
    {
        this.pathToDstDir = pathToDstDir;
        this.duration = duration;
        this.boundingBox = boundingBox;
    }

    @Override
    public void consumeStreams(List<Stream> streams)
    {
        // All stream type except video are disabled. just ignore
    }

    @Override
    public void consume(Frame frame)
    {
        // End of Stream
        if (frame == null)
        {
            return;
        }

        final BufferedImage subImage = boundingBox != null ?
            ImageHelper.getSubImage(frame.getImage(), boundingBox.x, boundingBox.y, boundingBox.width,
                boundingBox.height) : frame.getImage();

        final String filename = duration.get()
            + ".jpg";
        final Path output = pathToDstDir.resolve(filename);

        try
        {
            ImageIO.write(subImage, "jpg", output.toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
