package com.example.translator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

//Сервис для взаимодействия с внешним API перевода
@Service
public class ExternalTranslationService implements TranslationAPI {

    private final RestTemplate restTemplate;

    public ExternalTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translateWord(String word, String sourceLang, String targetLang) {
        // URL API перевода
        String url = "http://translate.google.ru/translate_a/t?client=x&text="+ word +"&hl=" + sourceLang + "&sl=en&tl=" + targetLang;
        //String url = "https://api.externaltranslation.com/translate?word=" + word + "&source=" + sourceLang + "&target=" + targetLang;
        return restTemplate.getForObject(url, String.class);
    }
}
