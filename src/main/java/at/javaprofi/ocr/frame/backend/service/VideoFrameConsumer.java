package at.javaprofi.ocr.frame.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.kokorin.jaffree.ffmpeg.Frame;
import com.github.kokorin.jaffree.ffmpeg.FrameConsumer;
import com.github.kokorin.jaffree.ffmpeg.Stream;

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

        String filename = "frame_" + num++ + ".png";
        Path output = pathToDstDir.resolve(filename);

        try
        {
            ImageIO.write(frame.getImage(), "png", output.toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
