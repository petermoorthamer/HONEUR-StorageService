package com.jnj.honeur.storage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StorageController {

    @RequestMapping("/storage")
    public String home() {
        return "index.html";
    }
}
