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
        fetchAvailableLanguages();
    }

    @PostConstruct
    private void fetchAvailableLanguages() {
        try {
            this.availableLanguages = getSupportedLanguages();
        } catch (Exception e) {
            logger.error("Failed to fetch available languages", e);
            this.availableLanguages = new TreeSet<>();
        }
    }
    @SneakyThrows
    // Повторяем при выбросе исключения. До 3-х повторений (по умолчанию) с увеличивающимся интервалом
    @Retryable(
            retryFor = { Exception.class },
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public TreeSet<String> getSupportedLanguages() {
        String target = "en"; // Specify the target language here

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("target", target);

        URI uri = builder.build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", "google-translator9.p.rapidapi.com");
        headers.set("x-rapidapi-key", rapidApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();

        logger.info("Received response: " + responseBody);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode languagesNode = rootNode.path("data").path("languages");

                List<Language> languagesList = objectMapper.readValue(
                        languagesNode.toString(), new TypeReference<>() {}
                );

                return languagesList.stream()
                        .map(Language::getLanguage)
                        .collect(Collectors.toCollection(TreeSet::new));
            } catch (IOException e) {
                throw new RuntimeException("Error parsing response", e);
            }
        } else {
            throw new RuntimeException("Error fetching languages: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    public boolean isNotSupported(String languageCode) {
        return !availableLanguages.contains(languageCode);
    }

    @Recover // Выполнится, если ни одна из попыток не сработает
    public TreeSet<String> recover(Exception e) {
        logger.error("Не удалось получить список языков после повторных попыток", e);
        return new TreeSet<>();
    }
}
