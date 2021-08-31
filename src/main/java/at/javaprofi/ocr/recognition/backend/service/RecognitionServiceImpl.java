package at.javaprofi.ocr.recognition.backend.service;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.javaprofi.ocr.frame.api.dto.WordContainer;
import at.javaprofi.ocr.io.api.dto.PathContainer;
import at.javaprofi.ocr.io.api.service.FileService;
import at.javaprofi.ocr.recognition.api.service.RecognitionService;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.OCRResult;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

@Service
public class RecognitionServiceImpl implements RecognitionService
{
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionServiceImpl.class);

    private final FileService fileService;

    @Autowired
    public RecognitionServiceImpl(FileService fileService)
    {
        this.fileService = fileService;
    }

    @Override
    public void extractTextFromFramesToJSON(String fileName, boolean hocr)
    {

        final PathContainer pathContainer =
            fileService.createDirectoriesAndRetrievePathContainerFromVideoFileName(fileName);

        final Stream<Path> pathStream = fileService.retrieveContainingFilesAsPathStream(
            pathContainer.getFramesPath());

        final long count = fileService.retrieveContainingFilesAsPathStream(
            pathContainer.getFramesPath()).count();

        final LongAdder longAdder = new LongAdder();
        longAdder.add(count);
        final List<WordContainer> wordContainerList = new ArrayList<>();

        LOG.info("extracting lines of: {}", fileName);

        final Stream<Path> parallelStream = pathStream.parallel();

        parallelStream.forEach(framePath -> {
            final Tesseract tesseract1 = getTesseractInstance(hocr);
            try
            {
                longAdder.decrement();
                if (longAdder.sum() % 10 == 0)
                {
                    LOG.info("extracted text from {} frames of {}", longAdder.sum(), count);
                }
                wordContainerList.add(retrieveWordContainerForFrame(tesseract1, framePath.toFile()));

            }
            catch (TesseractException e)
            {
                LOG.error("exception occurred during extracting linesy of frame: {}", framePath, e);
            }
        });
        writeWordContainerListToJSON(wordContainerList, pathContainer.getExtractedLinesPath());
    }

    private void writeWordContainerListToJSON(List<WordContainer> wordContainerList, Path visualizationPath)
    { //First Employee

        LOG.info("writing extracted lines to json file: {}", visualizationPath);

        //Write JSON file
        try (FileWriter file = new FileWriter(visualizationPath.toFile()))
        {
            final JSONArray extractedJSON = new JSONArray();

            wordContainerList.forEach(wordContainer -> {
                final JSONObject jsonWordContainer = new JSONObject();
                jsonWordContainer.put("duration", wordContainer.getDuration());

                final JSONArray wordContainerWordList = new JSONArray();

                for (Word word : wordContainer.getWordList())
                {
                    final JSONObject wordJson = new JSONObject();
                    final Rectangle boundingBox = word.getBoundingBox();

                    wordJson.put("text", word.getText());
                    wordJson.put("x", boundingBox.x);
                    wordJson.put("y", boundingBox.y);
                    wordJson.put("width", boundingBox.width);
                    wordJson.put("height", boundingBox.height);

                    wordContainerWordList.add(wordJson);
                }

                jsonWordContainer.put("wordList", wordContainerWordList);
                extractedJSON.add(jsonWordContainer);
            });

            file.write(extractedJSON.toJSONString());
            file.flush();

            LOG.info("finished writing json file: {}", visualizationPath);

        }
        catch (IOException e)
        {
            throw new RuntimeException("writing extracted lines to json failed", e);
        }
    }

    private WordContainer retrieveWordContainerForFrame(Tesseract tesseract1, File frameFile) throws
        TesseractException
    {
        final OCRResult documentsWithResults;
        final String absoluteFileString = frameFile.getAbsoluteFile().toString();
        final String duration = StringUtils.getDigits(frameFile.getName());
        documentsWithResults =
            tesseract1.createDocumentsWithResults(absoluteFileString, absoluteFileString,
                Collections.singletonList(ITesseract.RenderedFormat.HOCR),
                ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE);

        final WordContainer wordContainer = new WordContainer();
        if (StringUtils.isNoneBlank(duration))
        {
            wordContainer.setDuration(Long.parseLong(duration));
        }
        else
        {
            wordContainer.setDuration(11L);
        }
        wordContainer.setWordList(documentsWithResults.getWords());

        return wordContainer;
    }

    private Tesseract getTesseractInstance(boolean hocr)
    {
        final Tesseract tesseract = new Tesseract();

        tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setHocr(hocr);
        tesseract.setTessVariable("user_defined_dpi", "300");

        return tesseract;
    }
}
