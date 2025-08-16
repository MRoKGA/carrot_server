package com.mrokga.carrot_server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestApi {
    @GetMapping("/theTest")
    public String home(){
        return "hello world carrotcarrot real carrot";
    }

}
