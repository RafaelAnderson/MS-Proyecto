package com.ms.bank.account.service;

import com.ms.bank.account.model.ModelApiResponse;
import org.springframework.http.ResponseEntity;

public interface ReportsService {

    ResponseEntity<ModelApiResponse> generateProductReport(String clientId);
}
