package com.example.translator.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TranslationRequestBody {
    private String q;
    private String source;
    private String target;
    private String format;
}
