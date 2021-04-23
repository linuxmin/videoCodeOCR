package at.javaprofi.ocr.frame.backend.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FrameOutput;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;

import at.javaprofi.ocr.filestorage.api.dao.PathContainer;
import at.javaprofi.ocr.filestorage.api.service.FileStorageService;
import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import at.javaprofi.ocr.frame.api.word.WordContainer;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

@Service
public class FrameExtractorServiceImpl implements FrameExtractorService
{
    private static final List<String> VIDEO_FILTER = Arrays.asList(".mp4", ".m4v");
    private static final String[] HEADERS = {"framenumber", "x", "y", "text"};

    private static final Logger LOG = LoggerFactory.getLogger(FrameExtractorServiceImpl.class);

    private final FileStorageService fileStorageService;

    @Autowired
    public FrameExtractorServiceImpl(FileStorageService fileStorageService)
    {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void extractCodeFromVideo(String fileName, boolean hocr)
    {
        if (VIDEO_FILTER.stream().noneMatch(fileName::endsWith))
        {
            throw new RuntimeException("No video file provided");
        }

        final Tesseract tesseract = getTesseractInstance(hocr);

        final PathContainer pathContainer =
            fileStorageService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);

        final AtomicLong frameCounter = extractFramesToFramePathAndGetFrameCount(pathContainer);
        final Path framesPath = pathContainer.getFramesPath();
        final Path hocrPath = pathContainer.getHocrPath();

        try (final Stream<Path> framePathStream =
            fileStorageService.retrieveContainingFilesAsPathStream(framesPath))
        {
            framePathStream.forEach(framePath -> {
                doOCRandWriteHocrToXML(tesseract, hocrPath, framePath);
                LOG.info("Extracting next frame, {} frames left", frameCounter.decrementAndGet());
            });
        }

        LOG.info("Finished OCR of {}", fileName);
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

        final AtomicLong frameCounter = extractFramesToFramePathAndGetFrameCount(pathContainer);

        final List<WordContainer> wordContainerList = new ArrayList<>();

        try (final Stream<Path> framePathStream = fileStorageService.retrieveContainingFilesAsPathStream(
            pathContainer.getFramesPath()))
        {
            framePathStream.forEach(framePath -> {
                wordContainerList.add(retrieveWordContainerForFrame(tesseract, framePath.toFile()));
                LOG.info("Extracting next frame, {} frames left", frameCounter.decrementAndGet());
            });
        }

        FileWriter out = null;

        try
        {
            out = new FileWriter("capture3.csv");

            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader(HEADERS)))
            {
                wordContainerList.forEach((wordContainer) -> {

                    final long frameNumber = wordContainer.getFrameNumber();

                    wordContainer.getWordList().forEach(word ->
                        {
                            LOG.info("Adding csv record for frame: {}", frameNumber);
                            try
                            {
                                printer.printRecord(frameNumber, word.getBoundingBox().x, word.getBoundingBox().y,
                                    word.getText());
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    );
                });
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private AtomicLong extractFramesToFramePathAndGetFrameCount(PathContainer pathContainer)
    {
        final Path videoPath = pathContainer.getVideoPath();
        final Path framesPath = pathContainer.getFramesPath();

        final AtomicLong duration = new AtomicLong();
        final AtomicLong frameCount = new AtomicLong();

        //needed to get duration for word listener
        FFmpeg.atPath()
            .addInput(UrlInput.fromPath(videoPath))
            .setOverwriteOutput(true)
            .addOutput(new NullOutput())
            .setProgressListener(progress -> {
                duration.set(progress.getTimeMillis());
            })
            .execute();

        FFmpeg.atPath()
            .addInput(UrlInput
                .fromPath(videoPath))
            .addOutput(FrameOutput
                .withConsumer(new VideoFrameConsumer(framesPath))
                //.addArguments("-vsync", "vfr")  // sync framerate with input file
                .setFrameRate(5.381234473533346)
                .disableStream(StreamType.AUDIO)
                .disableStream(StreamType.SUBTITLE)
                .disableStream(StreamType.DATA))
            .setProgressListener(progress -> {
                double percents = 100. * progress.getTimeMillis() / duration.get();
                final BigDecimal percentage = BigDecimal.valueOf(percents).setScale(2, RoundingMode.HALF_UP);
                LOG.info("Frame extraction progress: {} %", percentage);
                frameCount.set(progress.getFrame());
            })
            .execute();

        return frameCount;

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

    private WordContainer retrieveWordContainerForFrame(Tesseract tesseract, File frameFile)
    {
        final OCRResult documentsWithResults;
        final String absoluteFileString = frameFile.getAbsoluteFile().toString();
        try
        {
            final String absoluteFile = absoluteFileString;
            documentsWithResults =
                tesseract.createDocumentsWithResults(absoluteFile, "test.xml",
                    Collections.singletonList(ITesseract.RenderedFormat.HOCR),
                    ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);
        }
        catch (TesseractException e)
        {
            throw new RuntimeException("Exception during OCR occurred", e);
        }

        final String frameNumberFromFile = StringUtils.getDigits(absoluteFileString);

        final WordContainer wordContainer = new WordContainer();
        wordContainer.setFrameNumber(Long.parseLong(frameNumberFromFile));
        wordContainer.setWordList(documentsWithResults.getWords());

        return wordContainer;
    }
}
