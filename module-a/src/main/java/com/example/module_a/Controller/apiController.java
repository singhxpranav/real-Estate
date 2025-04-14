package com.example.module_a.Controller;

import com.example.module_b.Service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class apiController {
    private final ExampleService exampleService;

    @Autowired
    public apiController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping("/data")
    public String getData() {
        return exampleService.getServiceData();
    }
}
