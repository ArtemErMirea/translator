package com.example.translator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class ExternalTranslationService {

    private final RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    public ExternalTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translateWord(String word, String sourceLang, String targetLang) {
        //String url = UriComponentsBuilder.fromHttpUrl("https://deep-translate1.p.rapidapi.com/language/translate/v2")
        String url = UriComponentsBuilder.fromHttpUrl("https://google-translator9.p.rapidapi.com/v2")
                .queryParam("q", word)
                .queryParam("source", sourceLang)
                .queryParam("target", targetLang)
                .queryParam("format", "text")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        //headers.set("x-rapidapi-host", "deep-translate1.p.rapidapi.com");
        headers.set("x-rapidapi-host", "google-translator9.p.rapidapi.com");
        headers.set("x-rapidapi-key", rapidApiKey);

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