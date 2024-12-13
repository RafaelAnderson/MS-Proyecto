package com.ms.bank.account.impl;

import com.ms.bank.account.api.ReportsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReportsApiDelegateImpl implements ReportsApiDelegate {

    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;


    public ReportsApiDelegateImpl(BankAccountRepository bankAccountRepository, CreditRepository creditRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.creditRepository = creditRepository;
    }

    @Override
    public ResponseEntity<ModelApiResponse> generateProductReport(String clientId) {

        List<Credit> creditAccountList = creditRepository.findByClientId(clientId);
        List<BankAccount> bankAccountList = bankAccountRepository.findByClientId(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("debitAccounts", bankAccountList);
        response.put("creditAccounts", creditAccountList);

        return ResponseUtil.getMapResponse(HttpStatus.OK.value(), "Products", response);
    }
}
