package com.example.translator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ExternalTranslationService {

    public static final Logger logger = LoggerFactory.getLogger(ExternalTranslationService.class);

    private final RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${baseUrl}")
    private String baseUrl;

    public ExternalTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Использованное Api имеет ограничение за количество запросов в секунду,
    // из-за чего оно может отказать в переводе слова и вызвать исключение.
    // Повторяем при выбросе исключения. До 3-х повторений с увеличивающимся интервалом
    @SneakyThrows
    @Retryable(
            retryFor = { Exception.class },
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public String translateWord(String word, String sourceLang, String targetLang) {

        // URI с параметрами запроса
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", word)
                .queryParam("source", sourceLang)
                .queryParam("target", targetLang);
        URI uri = builder.build().toUri();

        // Устанавливаем заголовки для запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-rapidapi-host", "google-translator9.p.rapidapi.com");
        headers.set("x-rapidapi-key", rapidApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.info("Посылаем запрос URL: " + uri);
        logger.info("Заголовки запросов: " + headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        String responseBody = response.getBody();

        logger.info("Ответ: " + responseBody);


        // JSON преобразован в объект JsonNode, который использован для извлечения данных
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode translations = root.path("data").path("translations");

        // Если результат перевода - непустой массив, то мы возвращаем значение в декодированном виде
        if (translations.isArray() && !translations.isEmpty()) {
            String translatedText = translations.get(0).path("translatedText").asText();
            return URLDecoder.decode(translatedText, StandardCharsets.UTF_8);
        } else {
            logger.warn("В ответе перевод не найден");
            return "";
        }
    }
}

