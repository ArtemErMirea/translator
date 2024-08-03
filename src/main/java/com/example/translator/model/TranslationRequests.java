package com.example.translator.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("translation_requests")
public class TranslationRequests {

    @Id
    private Long id;
    private String ipAddress; //IP-адрес
    private String inputString; // входная строка для перевода
    private String translatedString; // результат перевода
    private LocalDateTime requestTime; // дата и время перевода
}
