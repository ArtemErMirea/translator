package com.example.translator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.List;

@Service
public class ExternalTranslationService implements TranslationAPI {

    private final RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    public ExternalTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String translateWord(String word, String sourceLang, String targetLang) {
        String url = UriComponentsBuilder.fromHttpUrl("https://google-translate1.p.rapidapi.com/language/translate/v2")
                .queryParam("q", word)
                .queryParam("source", sourceLang)
                .queryParam("target", targetLang)
                .queryParam("format", "text")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("X-RapidAPI-Host", "google-translate1.p.rapidapi.com");
        headers.set("X-RapidAPI-Key", rapidApiKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        if (data != null) {
            Map<String, Object> translations = (Map<String, Object>) data.get("translations");
            if (translations != null && !translations.isEmpty()) {
                return (String) translations.get("translatedText");
            }
        }
        return null;
    }
}