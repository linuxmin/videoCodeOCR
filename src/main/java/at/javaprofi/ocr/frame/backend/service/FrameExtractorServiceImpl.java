package at.javaprofi.ocr.frame.backend.service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

@Service
public class FrameExtractorServiceImpl implements FrameExtractorService
{
    private static final List<String> VIDEO_FILTER = Arrays.asList(".mp4");
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
        if (VIDEO_FILTER.stream().noneMatch(fileName::endsWith))
        {
            throw new RuntimeException("No video file provided");
        }

        final Tesseract tesseract = getTesseractInstance(true);

        final PathContainer pathContainer =
            fileStorageService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);

        extractFramesToFramePath(pathContainer);

        final Path framesPath = pathContainer.getFramesPath();
        final Path hocrPath = pathContainer.getHocrPath();

        try (final Stream<Path> framePathStream =
            fileStorageService.retrieveContainingFilesAsPathStream(framesPath))
        {
            framePathStream.forEach(framePath -> doOCRandWriteHocrToXML(tesseract, hocrPath, framePath));
        }
    }

    private Tesseract getTesseractInstance(boolean hocr)
    {
        final Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setHocr(hocr);
        return tesseract;
    }

    @Override
    public List<Word> extractWordsFromVideo(String fileName)
    {
        final Tesseract tesseract = getTesseractInstance(false);
        final PathContainer pathContainer =
            fileStorageService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);

        extractFramesToFramePath(pathContainer);

        final List<Word> wordList = new ArrayList<>();

        try (final Stream<Path> framePathStream = fileStorageService.retrieveContainingFilesAsPathStream(
            pathContainer.getFramesPath()))
        {
            framePathStream.forEach(framePath -> wordList.addAll(retrieveWords(tesseract,framePath.toFile())));
        }

        return null;
    }

    private void extractFramesToFramePath(PathContainer pathContainer)
    {
        final Path videoPath = pathContainer.getVideoPath();
        final Path framesPath = pathContainer.getFramesPath();

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(videoPath))
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(framesPath))
                .setFrameCount(StreamType.VIDEO, 100L)
                .setFrameRate(1)
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA))
            .execute();
    }

    private void doOCRandWriteHocrToXML(Tesseract tesseract, Path hocrPath, Path framePath)
    {
        final File frameFile = framePath.toFile();
        String codeFromFrame;
        try
        {
            codeFromFrame = tesseract.doOCR(frameFile.getAbsoluteFile());
            LOG.info("Extracted frames from: {}", frameFile);
        }
        catch (TesseractException e)
        {
            throw new RuntimeException("Exception during OCR occurred", e);
        }
        fileStorageService.writeHocrToXML(codeFromFrame, hocrPath, framePath);
    }

    private List<Word> retrieveWords(Tesseract tesseract, File frameFile)
    {
        final OCRResult documentsWithResults;
        try
        {
            documentsWithResults =
                tesseract.createDocumentsWithResults(frameFile.getAbsoluteFile().toString(), "test.xml",
                    Collections.singletonList(ITesseract.RenderedFormat.HOCR), 1);
        }
        catch (TesseractException e)
        {
            throw new RuntimeException("Exception during OCR occurred", e);
        }

        return documentsWithResults.getWords();
    }
}
