package com.example.translator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

    private static final Logger logger = LoggerFactory.getLogger(ExternalTranslationService.class);
    //@Autowired private Environment env;

    private final RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    public ExternalTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        //this.rapidApiKey = env.getProperty("rapidapi.key");
    }

    public String translateWord(String word, String sourceLang, String targetLang) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://google-translator9.p.rapidapi.com/v2")
                    .queryParam("q", word)
                    .queryParam("source", sourceLang)
                    .queryParam("target", targetLang)
                    .queryParam("format", "text")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("x-rapidapi-host", "google-translator9.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("Sending request to URL: " + url);
            logger.info("Request headers: " + headers.toString());

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            logger.info("Received response: " + response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("data").path("translations").get(0).path("translatedText").asText();

        } catch (Exception e) {
            logger.error("Error during translation: ", e);
            return null;
        }
    }
}