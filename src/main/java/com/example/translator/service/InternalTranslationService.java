package com.example.translator.service;

import org.springframework.stereotype.Service;
import com.example.translator.model.TranslationRequests;
import com.example.translator.repository.TranslationRequestRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
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
        this.executor.setMaxPoolSize(20);  // Устанавливаем максимальный размер пула
        this.executor.setQueueCapacity(50); // Устанавливаем ёмкость очереди
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
        List<CompletableFuture<String>> futures = Stream.of(words)
                .map(word -> CompletableFuture.supplyAsync(
                        () -> retryWithBackoff(
                                () -> externalTranslationService.translateWord(word, sourceLang, targetLang)
                        ), executor
                )).collect(Collectors.toList());

        // Обработка всех результатов
        List<String> translatedWords = futures.stream()
                .map(this::getFutureResult)
                .collect(Collectors.toList());

        String translatedString = String.join(" ", translatedWords);

        // Сохраняем запрос в базу данных
        TranslationRequests request = new TranslationRequests(
                null, ipAddress, input, translatedString, LocalDateTime.now()
        );
        translationRequestRepository.save(request);

        return translatedString;
    }

    private String getFutureResult(CompletableFuture<String> future) {
        try {
            return future.get(); // Ожидание результата
        } catch (Exception e) {
            // Логирование ошибки и возвращение пустой строки
            System.err.println("Error while fetching translation result: " + e.getMessage());
            return "";
        }
    }
    private String retryWithBackoff(Supplier<String> task) {
        int attempt = 0;
        int maxRetries = 6;
        while (attempt < maxRetries) {
            try {
                return task.get();
            } catch (HttpClientErrorException.TooManyRequests e) {
                attempt++;
                try {
                    // Ожидание перед повторной попыткой
                    TimeUnit.SECONDS.sleep((long) Math.pow(2, attempt)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Failed after retries");
    }
}
