package com.example.translator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {

    @Id
    private Long id;
    private String ipAddress;
    private String inputString;
    private String translatedString;
    private LocalDateTime requestTime;
}
