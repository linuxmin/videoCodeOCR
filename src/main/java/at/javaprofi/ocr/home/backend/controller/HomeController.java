package at.javaprofi.ocr.home.backend.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import at.javaprofi.ocr.io.api.service.FileService;
import at.javaprofi.ocr.io.backend.controller.FileUploadController;

/**
 * controller used for navigation requests
 */
@Controller
public class HomeController
{
    private final FileService fileService;

    @Autowired
    public HomeController(FileService fileService)
    {
        this.fileService = fileService;
    }

    @GetMapping("/home")
    public String showHome(Model model)
    {
        model.addAttribute("files", fileService.loadVideos().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveFile", path.getFileName().toString()).build())
            .collect(Collectors.toList()));

        return "home";
    }


    @GetMapping("/codeviz")
    public String showVCodeVisualization(Model model)
    {
        model.addAttribute("files", fileService.loadVideos().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                    "serveFile", path.getFileName().toString()).build())
            .collect(Collectors.toList()));

        return "piechart";
    }

    @GetMapping("/animation")
    public String showAnimation(Model model)
    {

        model.addAttribute("files", fileService.loadVideos().map(
            path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                "serveFile", path.getFileName().toString()).build())
            .collect(Collectors.toList()));

        return "visualization";
    }
}
