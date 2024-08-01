package com.example.translator.service;

import org.springframework.stereotype.Service;
import com.example.translator.model.TranslationRequests;
import com.example.translator.repository.TranslationRequestRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Сервис для выполнения перевода и сохранения результатов в базе данных
@Service
public class InternalTranslationService {

    private final ExternalTranslationService externalTranslationService;
    private final TranslationRequestRepository translationRequestRepository;

    private final ThreadPoolTaskExecutor executor;

    public InternalTranslationService(ExternalTranslationService externalTranslationService,
                                      TranslationRequestRepository translationRequestRepository) {
        this.externalTranslationService = externalTranslationService;
        this.translationRequestRepository = translationRequestRepository;
        this.executor = new ThreadPoolTaskExecutor();
        // Задаём параметр, который показывает,
        // какое количество потоков будет готово (запущено)
        // при старте executor сервиса как 10.
        this.executor.setCorePoolSize(10);
        this.executor.initialize();
    }

    /**
     * Метод перевода
     * @param input Входная строка
     * @param sourceLang Исходный язык
     * @param targetLang Целевой язык
     * @param ipAddress IP-адрес клиента
     * @return Переведенная строка
     */
    public String translate(String input, String sourceLang, String targetLang, String ipAddress) {
        //Набор слов из строки переводим в массив строк
        String[] words = input.split(" ");
        //Список Future-ов - результатов параллельных вычислений
        // Созданы задачи для перевода каждого слова
        List<Future<String>> futures = Stream.of(words)
                .map(word -> executor.submit(
                        () -> externalTranslationService.translateWord(
                                word, sourceLang, targetLang)
                )).toList();

        // Собираем результаты переводов в единый String
        String translatedString = futures.stream()
                .map(this::getFutureResult)
                .collect(Collectors.joining(" "));

        // Сохраняем запрос в базу данных
        TranslationRequests request = new TranslationRequests(
                null, ipAddress, input, translatedString, LocalDateTime.now()
        );
        translationRequestRepository.save(request);

        return translatedString;
    }

    private String getFutureResult(Future<String> future) {
        try {
            // ждёт завершения расчётов и возвращает результат
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
