package com.example.translator.service;

import com.example.translator.model.TranslationRequests;
import com.example.translator.repository.TranslationRequestRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Сервис для выполнения перевода и сохранения результатов в базе данных
@Service
public class InternalTranslationService {

    private final ExternalTranslationService externalTranslationService;
    private final TranslationRequestRepository translationRequestRepository;

    private final ThreadPoolTaskExecutor executor;
    //private final RateLimiter rateLimiter;

    public InternalTranslationService(ExternalTranslationService externalTranslationService,
                                      TranslationRequestRepository translationRequestRepository) {
        this.externalTranslationService = externalTranslationService;
        this.translationRequestRepository = translationRequestRepository;
        this.executor = new ThreadPoolTaskExecutor();
        // Задаём параметр, который показывает,
        // какое максимальное количество потоков будет работать одновременно
        this.executor.setMaxPoolSize(10);
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
        //Список CompletableFuture-ов - результатов параллельных вычислений
        // Созданы задачи для перевода каждого слова
        List<CompletableFuture<String>> futures = Stream.of(words)
                .map(
                        word -> CompletableFuture.supplyAsync(
                        () -> externalTranslationService.translateWord(word, sourceLang, targetLang), executor)
                ).toList();

        // Обработка всех результатов
        List<String> translatedWords = futures.stream()
                .map(this::getFutureResult)
                .collect(Collectors.toList());

        // Результирующие слова в один String
        String translatedString = String.join(" ", translatedWords);

        // Сохраняем запрос в базу данных
        // id = nul, т.к. в БД поле формата bigserial, id будет создан автоматически
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
            System.err.println("Не удалось получить результат перевода: " + e.getMessage());
            return "";
        }
    }
}
