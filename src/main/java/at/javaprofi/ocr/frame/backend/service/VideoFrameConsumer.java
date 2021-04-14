package at.javaprofi.ocr.frame.backend.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.Stream;

import net.sourceforge.tess4j.util.ImageHelper;

public class VideoFrameConsumer implements FrameConsumer
{

    private long num = 1;
    private final Path pathToDstDir;

    public VideoFrameConsumer(Path pathToDstDir)
    {
        this.pathToDstDir = pathToDstDir;
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
        String filename = "frame_" + num++ + ".jpg";
        Path output = pathToDstDir.resolve(filename);

        try
        {
            ImageIO.write(invertedColor, "jpg", output.toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
