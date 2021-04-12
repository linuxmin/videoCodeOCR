package at.javaprofi.ocr.frame.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import at.javaprofi.ocr.filestorage.api.dao.PathContainer;
import at.javaprofi.ocr.filestorage.api.service.FileStorageService;
import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class FrameExtractorServiceImpl implements FrameExtractorService
{
    private static final List<String> VIDOE_FILTER = Arrays.asList(".mp4");
    private static final Logger LOG = LoggerFactory.getLogger(FrameExtractorServiceImpl.class);

    private final FileStorageService fileStorageService;

    @Autowired
    public FrameExtractorServiceImpl(FileStorageService fileStorageService)
    {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void extractCodeFromVideo(String fileName)
    {
       if (VIDOE_FILTER.stream().noneMatch(fileName::endsWith))
        {
            throw new RuntimeException("No video file provided");

        }


        final Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setHocr(true);

        final PathContainer pathContainer =
            fileStorageService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);
        final Path videoPath = pathContainer.getVideoPath();
        final Path framesPath = pathContainer.getFramesPath();

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(videoPath))
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(framesPath))
                // No more then 100 frames
                .setFrameCount(StreamType.VIDEO, 2L)
                // 5 frame every second
                .setFrameRate(5)
                // Disable all streams except video
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA))
            .execute();

        final Path hocrPath = pathContainer.getHocrPath();

        try (final Stream<Path> framePathStream =
            fileStorageService.retrieveContainingFilesAsPathStream(framesPath))
        {
            framePathStream.forEach(framePath -> {
                try
                {
                    final File frameFile = framePath.toFile();
                    String codeFromFrame = tesseract.doOCR(frameFile.getAbsoluteFile());
                    fileStorageService.writeHocrToXML(codeFromFrame, hocrPath, framePath);
                    LOG.info("Extracted frames from: {}", frameFile);

                }
                catch (TesseractException e)
                {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
