package com.ms.bank.account.service;

import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface CreditService {

    Mono<ResponseEntity<ModelApiResponse>> createCredit(Credit credit);

    Mono<ResponseEntity<Void>> deleteCredit(String creditId);

    Mono<ResponseEntity<ModelApiResponse>> getCreditById(String creditId);

    Mono<ResponseEntity<ModelApiResponse>> getAllCredits();

    Mono<ResponseEntity<ModelApiResponse>> getCreditsByClientId(String clientId);
}
