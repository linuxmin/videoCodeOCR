package at.javaprofi.ocr.frame.backend.service;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import at.javaprofi.ocr.io.api.dto.PathContainer;
import at.javaprofi.ocr.io.api.service.FileService;

@Service
public class FrameExtractorServiceImpl implements FrameExtractorService
{
    private static final List<String> VIDEO_FILTER = Arrays.asList(".mp4", ".m4v");

    private static final Logger LOG = LoggerFactory.getLogger(FrameExtractorServiceImpl.class);

    private final FileService fileService;

    @Autowired
    public FrameExtractorServiceImpl(FileService fileService)
    {
        this.fileService = fileService;
    }

    @Override
    public void extractFrames(String fileName, Rectangle boundingBox)
    {
        if (VIDEO_FILTER.stream().noneMatch(fileName::endsWith))
        {
            throw new RuntimeException("No video file provided");
        }

        final PathContainer pathContainer =
            fileService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);

        final AtomicLong frameCounter = new AtomicLong();

        final long startTime = System.currentTimeMillis();
        LOG.info("Started operation at: {}", startTime);

        countFramesAndExtractAsyncToFramePath(pathContainer, frameCounter, boundingBox);
    }

    private void countFramesAndExtractAsyncToFramePath(PathContainer pathContainer, AtomicLong frameCount,
        Rectangle boundingBox)
    {
        final Path videoPath = pathContainer.getVideoPath();
        final Path framesPath = pathContainer.getFramesPath();

        final AtomicLong duration = new AtomicLong();

        //needed to get duration for dto listener
        FFmpeg.atPath()
            .addInput(UrlInput.fromPath(videoPath))
            .setOverwriteOutput(true)
            .addOutput(new NullOutput())
            .setProgressListener(progress -> {
                duration.set(progress.getTimeMillis());
            }).execute();

        AtomicLong currentTimeMillis = new AtomicLong();

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(videoPath)).setFilter(StreamType.VIDEO, "negate")
            .setProgressListener(progress -> {
                frameCount.set(progress.getFrame());
                currentTimeMillis.set(progress.getTimeMillis());
                double percents = 100. * currentTimeMillis.get() / duration.get();
                final BigDecimal percentage = BigDecimal.valueOf(percents).setScale(2, RoundingMode.HALF_UP);
                LOG.info("Frame extraction progress:y {} %", percentage);
            })
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(framesPath, frameCount, boundingBox))
                .setFrameRate(60)
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA))
            .execute();
    }
}
