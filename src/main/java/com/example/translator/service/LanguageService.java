package com.example.translator.service;

import com.example.translator.model.Language;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class LanguageService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalTranslationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${languageUrl}")
    private String baseUrl;

    private TreeSet<String> availableLanguages;

    @Autowired
    public LanguageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Инициализируем список доступных языков при создании экземпляра сервиса
        fetchAvailableLanguages();
    }

    @PostConstruct
    private void fetchAvailableLanguages() {
        try {
            // Получаем список поддерживаемых языков
            this.availableLanguages = getSupportedLanguages();
        } catch (Exception e) {
            // Логируем ошибку при неудачной попытке получить список языков
            logger.error("Не удалось получить список языков", e);
            this.availableLanguages = new TreeSet<>();
        }
    }

    @SneakyThrows
    // Повторяем при выбросе исключения. До 3-х повторений с увеличивающимся интервалом
    @Retryable(
            retryFor = { Exception.class },
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public TreeSet<String> getSupportedLanguages() {
        String target = "en"; // Указываем целевой язык

        // URI с параметрами запроса
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("target", target);

        URI uri = builder.build().toUri();

        // Устанавливаем заголовки для запроса
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", "google-translator9.p.rapidapi.com");
        headers.set("x-rapidapi-key", rapidApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Выполняем запрос и получаем ответ
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();

        // Логируем полученный ответ
        logger.info("Received response: " + responseBody);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                // Парсим ответ и извлекаем список языков
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode languagesNode = rootNode.path("data").path("languages");

                List<Language> languagesList = objectMapper.readValue(
                        languagesNode.toString(), new TypeReference<>() {}
                );

                // Преобразуем список языков в TreeSet
                return languagesList.stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toCollection(TreeSet::new));
            } catch (IOException e) {
                // Бросаем исключение при ошибке парсинга
                throw new RuntimeException("Ошибка парсинга", e);
            }
        } else {
            // Бросаем исключение при неудачной попытке получить список языков
            throw new RuntimeException("Не удалось получить список языков: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    // Проверяем, поддерживается ли данный язык
    public boolean isNotSupported(String languageCode) {
        return !availableLanguages.contains(languageCode);
    }

    @Recover // Выполнится, если ни одна из попыток не сработает
    public TreeSet<String> recover(Exception e) {
        logger.error("Не удалось получить список языков после повторных попыток", e);
        return new TreeSet<>();
    }
}
