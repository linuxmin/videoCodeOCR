package at.javaprofi.ocr.filestorage.backend.controller;

import java.net.MalformedURLException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import at.javaprofi.ocr.filestorage.api.service.FileStorageService;

@Controller
public class FileUploadController
{
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

    private final FileStorageService fileStorageService;

    @Autowired
    public FileUploadController(FileStorageService fileStorageService)
    {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/video")
    public String listUploadedFiles(Model model)
    {
        model.addAttribute("files", fileStorageService.loadVideos().map(
            path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                "serveFile", path.getFileName().toString()).build())
            .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename)
    {
        try
        {
            Resource file = fileStorageService.loadAsResource(filename);

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/video")
    public String handleFileUpload(@RequestParam("file") MultipartFile file)
    {
        fileStorageService.store(file);

        return "redirect:/video";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleFileUploadExceptions(RedirectAttributes redirectAttributes, RuntimeException ex)
    {
        LOG.error("Exception occurred while handling files/filestorage with following message: {}", ex.getMessage());
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("message",
            ex.getMessage());

        return "redirect:/video";
    }
}
