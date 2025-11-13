package ru.practicum.shareit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "message", "ShareIt Server is running",
                "port", "9090",
                "status", "OK"
        );
    }
}