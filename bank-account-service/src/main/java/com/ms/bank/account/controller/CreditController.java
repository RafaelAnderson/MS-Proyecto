package com.ms.bank.account.controller;

import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.service.CreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping
    public Mono<ResponseEntity<ModelApiResponse>> createCredit(@RequestBody Credit credit) {
        return creditService.createCredit(credit);
    }

    @DeleteMapping("/{creditId}")
    public Mono<ResponseEntity<Void>> deleteClient(@PathVariable String creditId) {
        return creditService.deleteCredit(creditId);
    }

    @GetMapping("/{creditId}")
    public Mono<ResponseEntity<ModelApiResponse>> getCreditById(@PathVariable String creditId) {
        return creditService.getCreditById(creditId);
    }

    @GetMapping
    public Mono<ResponseEntity<ModelApiResponse>> getAllCredits() {
        return creditService.getAllCredits();
    }

    @GetMapping("/client/{clientId}")
    public Mono<ResponseEntity<ModelApiResponse>> getCreditsByClientId(@PathVariable String clientId) {
        return creditService.getCreditsByClientId(clientId);
    }
}
