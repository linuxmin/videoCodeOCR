import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import at.javaprofi.ocr.frame.backend.service.VideoFrameConsumer;

public class JaffreeTest
{

    @Test
    public void extractFrames()
    {
        final String userRunDir = System.getProperties().getProperty("user.dir");
        final String pathToSource = userRunDir + "/src/test/resources/test3.mp4";
        final String pathToDest = userRunDir + "/src/test/resources/";

        final Path pathToSrc = Paths.get(pathToSource);
        final Path pathToDstDir = Paths.get(pathToDest);

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(pathToSrc)
            )
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(pathToDstDir, new AtomicLong(), null))
                // No more then 100 frames
                .setFrameCount(StreamType.VIDEO, 100L)
                // 20 frame every quarter second
                .setFrameRate(5)
                // Disable all streams except video
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA)
            ).execute();
    }
}

