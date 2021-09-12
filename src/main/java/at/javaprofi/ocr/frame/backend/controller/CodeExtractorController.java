package at.javaprofi.ocr.frame.backend.controller;

import java.awt.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import at.javaprofi.ocr.parsing.api.service.ParsingService;
import at.javaprofi.ocr.recognition.api.service.RecognitionService;

/**
 * main controller, handling and forwarding API requests to the proper services
 */
@Controller
public class CodeExtractorController
{
    private static final Logger LOG = LoggerFactory.getLogger(CodeExtractorController.class);

    private final FrameExtractorService frameExtractorService;
    private final ParsingService parsingService;
    private final RecognitionService recognitionService;

    @Autowired
    public CodeExtractorController(FrameExtractorService frameExtractorService,
        ParsingService parsingService, RecognitionService recognitionService)
    {
        this.frameExtractorService = frameExtractorService;
        this.parsingService = parsingService;
        this.recognitionService = recognitionService;
    }

    @RequestMapping(value = "/frame")
    public String handleFrameExtraction(@RequestParam String fileName, @RequestParam Integer x, @RequestParam Integer y,
        @RequestParam Integer width, @RequestParam Integer height)
    {
        frameExtractorService.extractFrames(fileName, new Rectangle(x, y, width, height));
        return "redirect:/video";
    }

    @RequestMapping(value = "/ocr")
    public String handlePerformOCR(@RequestParam String fileName)
    {
        recognitionService.extractTextFromFramesToJSON(fileName, true);
        return "redirect:/video";
    }

    @RequestMapping(value = "/classMethodJSON")
    public String handleCreateClassMethodJSON(@RequestParam String fileName)
    {
        parsingService.parsingOriginalSourceCodeAndWriteCalculatedMatchingLinesToJSONFiles(fileName);
        return "redirect:/video";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleExtractionExceptions(RedirectAttributes redirectAttributes, RuntimeException ex)
    {
        LOG.error("Exception occurred while code extraction process with following message: {}", ex.getMessage());
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("message",
            ex.getMessage());
        return "redirect:/video";
    }

}
