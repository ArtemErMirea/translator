package com.example.translator.repository;


import com.example.translator.model.TranslationRequest;
import org.springframework.data.repository.CrudRepository;

public interface TranslationRequestRepository extends CrudRepository<TranslationRequest, Long> {
}
