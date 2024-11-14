package com.ms.bank.account.impl;

import com.ms.bank.account.api.AccountsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.TransactionRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BankAccountApiDelegateImpl implements AccountsApiDelegate {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ResponseEntity<ModelApiResponse> createAccount(BankAccount bankAccount) {


        List<BankAccount> existingAccount = bankAccountRepository.findByClientId(bankAccount.getClientId()).stream()
                .filter(bk -> bk.getType().getValue().equals(bankAccount.getType().getValue()))
                .collect(Collectors.toList());

        if (!existingAccount.isEmpty()) {
            return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(), "The customer already has an account of type " + bankAccount.getType(),
                    null);
        }

        bankAccount.setBalance(0d);
        BankAccount createdAccount = bankAccountRepository.save(bankAccount);

        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), "Account created successfully", createdAccount);
    }

    @Override
    public ResponseEntity<Void> deleteAccount(String accountId) {
        if (!bankAccountRepository.existsById(accountId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        bankAccountRepository.deleteById(accountId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAccountById(String accountId) {
        Optional<BankAccount> account = bankAccountRepository.findById(accountId);

        return account
                .map(bankAccount -> ResponseUtil.getResponse(HttpStatus.OK.value(), "Account", bankAccount))
                .orElseGet(() -> ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Account", null));
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAllAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();

        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of bank accounts", bankAccounts);
    }
}
