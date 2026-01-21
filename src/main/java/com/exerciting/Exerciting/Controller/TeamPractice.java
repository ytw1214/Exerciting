package com.exerciting.Exerciting.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/team")
public class TeamPractice {

    @GetMapping("/test")
    @ResponseBody // HTML 무시하고 글자만 띄우기
    public String test() {
        return "Controller is working!";
    }
}