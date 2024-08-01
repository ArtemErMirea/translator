package com.example.translator.controller;

import com.example.translator.service.InternalTranslationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translate")
public class TranslationController {

    private final InternalTranslationService translationService;

    public TranslationController(InternalTranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public String translate(
            @RequestParam String input,
            @RequestParam String sourceLang,
            @RequestParam String targetLang,
            @RequestHeader(value = "X-Forwarded-For", defaultValue = "127.0.0.1") String ipAddress) {
        return translationService.translate(input, sourceLang, targetLang, ipAddress);
    }
}
