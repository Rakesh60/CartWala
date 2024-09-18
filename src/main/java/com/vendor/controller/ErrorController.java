package com.vendor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController{

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
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request ,Model M) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
            	M.addAttribute("errorMessage","The Page you are looking not found");
            	M.addAttribute("title","Error "+ statusCode);
                return "error"; // Return your custom 404 page
            }
        }
        return "error"; // Generic error page
    }

    public String getErrorPath() {
        return "/error";
    }
    
}
