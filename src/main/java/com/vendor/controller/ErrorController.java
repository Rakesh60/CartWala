package com.vendor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
    	model.addAttribute("title","Access Denied");
        model.addAttribute("errorMessage", "You do not have permission to access this page.");
        return "error";
    }
    @GetMapping("/page404")
    public String pagenot(Model model) {
    	model.addAttribute("title","Page Not Found 404");
    	model.addAttribute("errorMessage", "Oops! The page you are looking for does not exist.");
    	return "error";
    }
    
    
}
