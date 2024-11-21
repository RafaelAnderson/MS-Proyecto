package com.ms.bank.account.impl;

import com.ms.bank.account.api.AccountsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.TransactionRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankAccountApiDelegateImpl implements AccountsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountApiDelegateImpl.class);

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ResponseEntity<ModelApiResponse> createAccount(BankAccount bankAccount) {
        logger.info("Request received to create a new bank account for client ID: {}", bankAccount.getClientId());

        List<BankAccount> existingAccount = bankAccountRepository.findByClientId(bankAccount.getClientId()).stream()
                .filter(bk -> bk.getType().getValue().equals(bankAccount.getType().getValue()))
                .collect(Collectors.toList());

        if (!existingAccount.isEmpty()) {
            logger.warn("Client ID: {} already has an account of type {}", bankAccount.getClientId(), bankAccount.getType());
            return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(),
                    "The customer already has an account of type " + bankAccount.getType(), null);
        }

        bankAccount.setBalance(0d);
        BankAccount createdAccount = bankAccountRepository.save(bankAccount);
        logger.info("Bank account created successfully with ID: {}", createdAccount.getId());

        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), "Account created successfully", createdAccount);
    }

    @Override
    public ResponseEntity<Void> deleteAccount(String accountId) {
        logger.info("Request received to delete bank account with ID: {}", accountId);

        if (!bankAccountRepository.existsById(accountId)) {
            logger.warn("Bank account with ID: {} not found", accountId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bankAccountRepository.deleteById(accountId);
        logger.info("Bank account with ID: {} deleted successfully", accountId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAccountById(String accountId) {
        logger.info("Fetching bank account with ID: {}", accountId);

        Optional<BankAccount> account = bankAccountRepository.findById(accountId);

        return account
                .map(bankAccount -> {
                    logger.info("Bank account found: {}", bankAccount.getId());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "Account", bankAccount);
                })
                .orElseGet(() -> {
                    logger.warn("Bank account with ID: {} not found", accountId);
                    return ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Account", null);
                });
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAllAccounts() {
        logger.info("Request received to fetch all bank accounts");
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();

        logger.info("Total bank accounts found: {}", bankAccounts.size());
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of bank accounts", bankAccounts);
    }
}
