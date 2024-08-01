package com.example.translator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
//Сервис для взаимодействия с внешним API перевода
@Service
public class GoogleCloudTranslationService implements TranslationAPI {

    @Value("${spring.cloud.gcp.translate.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GoogleCloudTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String translateWord(String word, String sourceLang, String targetLang) {
        //
        String url = UriComponentsBuilder.fromHttpUrl("https://translation.googleapis.com/language/translate/v2")
                .queryParam("q", word)
                .queryParam("source", sourceLang)
                .queryParam("target", targetLang)
                .queryParam("key", apiKey)
                .toUriString();

        // Выполнение запроса и получение ответа
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // Обработка ответа
        Map<String, Object> data = (Map<String, Object>) ((List<Object>) response.get("data")).get(0);
        return (String) data.get("translatedText");
    }
}
