package com.example.translator.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TranslationRequestBody {
    private String q; // Переводимое слово
    private String source; // Исходный язык
    private String target; // Язык перевода
    private String format; // Формат
}
