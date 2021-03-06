package at.javaprofi.ocr.io.backend.controller;

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

import at.javaprofi.ocr.io.api.service.FileService;

/**
 * controller handling API requests regarding file handling (upload, download...)
 */
@Controller
public class FileUploadController
{
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

    private final FileService fileService;

    @Autowired
    public FileUploadController(FileService fileService)
    {
        this.fileService = fileService;
    }

    @GetMapping("/video")
    public String listUploadedFiles(Model model)
    {
        model.addAttribute("files", fileService.loadVideos().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveFile", path.getFileName().toString()).build())
            .collect(Collectors.toList()));

        model.addAttribute("vizFiles", fileService.loadVisualizationPathsForVideo().stream().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveVizFile", path.toString()).build())
            .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename)
    {
        try
        {
            Resource file = fileService.loadVideoAsResource(filename);

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/vizFiles/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveVizFile(@PathVariable String filename)
    {
        try
        {
            Resource file = fileService.loadVizDataFileAsResource(filename);

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
        fileService.store(file);

        return "redirect:/video";
    }

    @PostMapping("/pupil")
    public String handlePupilDataUpload(@RequestParam("file") MultipartFile file,
        @RequestParam("fileName") String fileName)
    {
        fileService.storePupilFile(file, fileName);

        return "redirect:/video";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleFileUploadExceptions(RedirectAttributes redirectAttributes, RuntimeException ex)
    {
        LOG.error("Exception occurred while handling files/io with following message: {}", ex.getMessage());
        ex.printStackTrace();
        redirectAttributes.addFlashAttribute("message",
            ex.getMessage());

        return "redirect:/video";
    }

    @PostMapping(value = "/dropPupilFiles")
    public String handlePupilFilesDeletion(@RequestParam("fileName") String fileName)
    {
        fileService.deletePupilFilesForVideo(fileName);

        return "redirect:/video";
    }

    @PostMapping(value = "/dropVideo")
    public String handleVideoDeletion(@RequestParam("fileName") String fileName)
    {
        fileService.deleteVideo(fileName);

        return "redirect:/video";
    }
}
