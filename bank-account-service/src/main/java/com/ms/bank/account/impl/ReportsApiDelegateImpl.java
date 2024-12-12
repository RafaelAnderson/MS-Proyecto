package com.ms.bank.account.impl;

import com.ms.bank.account.api.ReportsApiDelegate;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ReportsApiDelegateImpl implements ReportsApiDelegate {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ResponseEntity<ModelApiResponse> generateProductReport(LocalDate startDate, LocalDate endDate) {

        return ResponseEntity.ok(null);
    }
}
