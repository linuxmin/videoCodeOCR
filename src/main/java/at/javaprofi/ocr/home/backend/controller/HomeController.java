package at.javaprofi.ocr.home.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * controller used for navigation requests
 */
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
        return "graphslider";
    }

    @GetMapping("/codeviz")
    public String showVCodeVisualization()
    {
        return "piechart";
    }

    @GetMapping("/animation")
    public String showAnimation()
    {
        return "visualization";
    }

    @GetMapping("/testviz")
    public String showResults()
    {
        return "testviz";
    }
}
