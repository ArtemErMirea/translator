package com.example.translator.controller;

import com.example.translator.service.InternalTranslationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translate")
@AllArgsConstructor
public class TranslationController {

    private final InternalTranslationService translationService;
    @PostMapping
    public ResponseEntity<String> translate(@RequestBody TranslationRequestBody request,
                                            @RequestHeader(value = "X-Forwarded-For", defaultValue = "127.0.0.1") String ipAddress) {
        String translatedText = translationService.translate(request.getQ(), request.getSource(), request.getTarget(), ipAddress);
        return ResponseEntity.ok(translatedText);
    }
}
