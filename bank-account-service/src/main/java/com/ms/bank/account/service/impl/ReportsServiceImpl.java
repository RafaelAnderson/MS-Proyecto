package com.ms.bank.account.service.impl;

import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.service.ReportsService;
import com.ms.bank.account.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportsServiceImpl implements ReportsService {

    private static final Logger logger = LoggerFactory.getLogger(ReportsServiceImpl.class);

    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;

    public ReportsServiceImpl(BankAccountRepository bankAccountRepository, CreditRepository creditRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.creditRepository = creditRepository;
    }

    @Override
    public ResponseEntity<ModelApiResponse> generateProductReport(String clientId) {

        List<Credit> creditAccountList = creditRepository.findByClientId(clientId);
        List<BankAccount> bankAccountList = bankAccountRepository.findByClientId(clientId);

        Map<String, Object> response = new HashMap<>();

        List<BankAccount> activeBankAccounts = bankAccountList.stream()
                .filter(account -> account.getBalance() > 0)
                .collect(Collectors.toList());

        List<Credit> activeCredits = creditAccountList.stream()
                .filter(credit -> credit.getStatus() == Credit.StatusEnum.ACTIVE)
                .collect(Collectors.toList());

        response.put("debitAccounts", activeBankAccounts);
        response.put("creditAccounts", activeCredits);

        logger.info("Generated product report successfully for clientId: {}", clientId);
        return ResponseUtil.getMapResponse(HttpStatus.OK.value(), "Products", response);
    }
}
