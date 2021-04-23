package at.javaprofi.ocr.home.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController
{

    @GetMapping("/home")
    public String showHome()
    {
        return "home";
    }

    @GetMapping("/visualization")
    public String showVisualization()
    {
        return "visualization";
    }


    @GetMapping("/codeviz")
    public String showVCodeVisualization()
    {
        return "codeviz";
    }

    @GetMapping("/animation")
    public String showAnimation()
    {
        return "animation";
    }
}
