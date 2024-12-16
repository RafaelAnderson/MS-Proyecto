package com.ms.bank.account.controller;

import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.service.impl.ReportsServiceImpl;
import com.ms.bank.account.util.ResponseUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsServiceImpl reportsApiDelegate;

    public ReportsController(ReportsServiceImpl reportsApiDelegate) {
        this.reportsApiDelegate = reportsApiDelegate;
    }

    @CircuitBreaker(name = "clientsCB", fallbackMethod = "fallBackGetProductReport")
    @GetMapping("/generate-product-report/{clientId}")
    public ResponseEntity<ModelApiResponse> generateProductReport(@PathVariable String clientId) {
        return reportsApiDelegate.generateProductReport(clientId);
    }

    public ResponseEntity<ModelApiResponse> fallBackGetProductReport(@PathVariable String clientId, RuntimeException e) {
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of products for " + clientId + " client", null);
    }
}
