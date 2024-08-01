package com.example.translator.repository;


import com.example.translator.model.TranslationRequests;
import org.springframework.data.repository.CrudRepository;

public interface TranslationRequestRepository extends CrudRepository<TranslationRequests, Long> {
}
