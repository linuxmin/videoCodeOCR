package at.javaprofi.ocr.frame.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import at.javaprofi.ocr.upload.api.UploadService;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class FrameExtractorServiceImpl implements FrameExtractorService
{
    private static final List<String> VIDOE_FILTER = Arrays.asList(".mp4");
    private static final Logger LOG = LoggerFactory.getLogger(FrameExtractorServiceImpl.class);

    private final UploadService uploadService;

    @Autowired
    public FrameExtractorServiceImpl(UploadService uploadService)
    {
        this.uploadService = uploadService;
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

        final Path fullPathToVideo = uploadService.load(fileName);
        final String directoryName = StringUtils.remove(fullPathToVideo.getFileName().toString(), ".");
        final Path pictureDestinationDir = Paths.get("extracted-dir", directoryName, "png");
        final Path hocrDestinationDir = Paths.get("extracted-dir", directoryName, "xml");
        try (final Stream<Path> framePathStream = extractFrames(fullPathToVideo,pictureDestinationDir, hocrDestinationDir))
        {
            framePathStream.forEach(path -> {
                try
                {

                    final File pictureFile = Paths.get(pictureDestinationDir.toString(),path.toString()).toFile();
                    String codeFromFrame = tesseract.doOCR(pictureFile.getAbsoluteFile());
                    try
                    {
                        final File hocrFile = Paths.get(hocrDestinationDir.toString(),path.toString()).toFile();
                        FileUtils.writeStringToFile(new File(hocrFile + ".xml"), codeFromFrame,
                            "UTF-8");
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }


                    LOG.info("Extracted frames from: {}", pictureFile);

                }
                catch (TesseractException e)
                {
                    throw new RuntimeException(e);
                }

            });
        }

    }

    private Stream<Path> extractFrames(Path fullPathToVideo, Path pathToDstDir, Path hocrDestinationDir)
    {
        final Path load = uploadService.load(fullPathToVideo.getFileName().toString());
        final String userRunDir = System.getProperties().getProperty("user.dir");
        final File file = new File(userRunDir + "/upload-dir/" + load.getFileName().toFile());

        final String pathToSource = file.getPath();
        final Path pathToSrc = Paths.get(pathToSource);

        try
        {
            Files.createDirectories(pathToDstDir);
            Files.createDirectories(hocrDestinationDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(pathToSrc)
            )
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(pathToDstDir))
                // No more then 100 frames
                .setFrameCount(StreamType.VIDEO, 20L)
                // 20 frame every quarter second
                .setFrameRate(5)
                // Disable all streams except video
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA)
            ).execute();

        try
        {
            return Files.walk(pathToDstDir, 1)
                .filter(path -> !path.equals(pathToDstDir))
                .map(pathToDstDir::relativize);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Bled", e);
        }
    }
}
