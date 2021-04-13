package at.javaprofi.ocr.frame.backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import net.sourceforge.tess4j.Word;

@Controller
public class CodeExtractorController
{
    private static final Logger LOG = LoggerFactory.getLogger(CodeExtractorController.class);

    private final FrameExtractorService frameExtractorService;

    @Autowired
    public CodeExtractorController(FrameExtractorService frameExtractorService)
    {
        this.frameExtractorService = frameExtractorService;
    }

    @RequestMapping(value = "/extract")
    public String handleCodeExtraction(@RequestParam String fileName)
    {
        frameExtractorService.extractCodeFromVideo(fileName);
        return "redirect:/video";
    }

    @RequestMapping(value = "/visualize")
    public String handleVisualization(@RequestParam String fileName, RedirectAttributes attributes)
    {
        final List<Word> wordList = frameExtractorService.extractWordsFromVideo(fileName);
        attributes.addFlashAttribute("wordList", wordList);
        return "redirect:/home";
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
