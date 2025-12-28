package com.sonyan.ollamahelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class OllamaHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(OllamaHelperApplication.class, args);
    }


    @GetMapping("/test")
    public String test() {
        return "hello world";
    }

}
