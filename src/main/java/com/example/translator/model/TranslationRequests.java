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
    private String ipAddress;
    private String inputString;
    private String translatedString;
    private LocalDateTime requestTime;
}
