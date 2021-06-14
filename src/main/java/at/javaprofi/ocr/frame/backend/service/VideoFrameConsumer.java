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

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;
import net.sourceforge.tess4j.util.ImageIOHelper;

/**
 * custom implementation of FrameConsumer
 * used by FFMpeg instance to scale and write its consumed frames to *.jpeg files
 */
public class VideoFrameConsumer implements FrameConsumer
{

    private final Path pathToDstDir;
    private final AtomicLong duration;

    public VideoFrameConsumer(Path pathToDstDir, AtomicLong duration)
    {
        this.pathToDstDir = pathToDstDir;
        this.duration = duration;
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

        final BufferedImage invertedColor = ImageHelper.invertImageColor(frame.getImage());
        final BufferedImage subImage = ImageHelper.getSubImage(invertedColor, 372, 83, 810, 776);
        String filename = duration.get()
            + ".jpg";
        Path output = pathToDstDir.resolve(filename);

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
