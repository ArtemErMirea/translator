package com.example.translator.controller;

import com.example.translator.service.InternalTranslationService;
import com.example.translator.service.LanguageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.TreeMap;

import static com.example.translator.service.ExternalTranslationService.logger;

@RestController
@RequestMapping
@AllArgsConstructor
public class TranslationController {

    private final LanguageService languageService;
    private final InternalTranslationService translationService;

    @GetMapping("/languages")
    public ResponseEntity<TreeMap<String, String>> getAvailableLanguages() {
        try {
            TreeMap<String, String> languages = languageService.getSupportedLanguages();
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            // логирование ошибки
            logger.error("Ошибка доступа к ресурсу получения списка языков", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestBody TranslationRequestBody request,
                                            @RequestHeader(value = "X-Forwarded-For", defaultValue = "127.0.0.1") String ipAddress) {
        if (languageService.isNotSupported(request.getSource())) {
            return ResponseEntity.badRequest().body("Не найден язык исходного сообщения");
        }

        if (languageService.isNotSupported(request.getTarget())) {
            return ResponseEntity.badRequest().body("Не найден целевой язык сообщения");
        }
        try {
            String translatedText = translationService.translate(request.getQ(), request.getSource(), request.getTarget(), ipAddress);
            return ResponseEntity.ok(translatedText);
        } catch (Exception e) {
            // логирование ошибки
            logger.error("Ошибка доступа к ресурсу перевода ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка доступа к ресурсу перевода");
        }
    }
}
