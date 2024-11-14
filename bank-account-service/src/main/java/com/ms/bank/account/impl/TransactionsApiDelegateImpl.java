package com.ms.bank.account.impl;

import com.ms.bank.account.api.TransactionsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.model.Transaction;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.TransactionRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionsApiDelegateImpl implements TransactionsApiDelegate {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional
    public ResponseEntity<ModelApiResponse> createTransaction(Transaction transaction) {

        if (!bankAccountRepository.existsById(transaction.getAccountId())) {
            return ResponseUtil.getResponse(HttpStatus.NO_CONTENT.value(), "Error getting account", null);
        }

        BankAccount bankAccount = bankAccountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (transaction.getAmount() <= 0) {
            return ResponseUtil.getResponse(HttpStatus.BAD_REQUEST.value(), "Invalid amount", null);
        }

        if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
            bankAccount.setBalance(bankAccount.getBalance() + transaction.getAmount());
        } else if (transaction.getType() == Transaction.TypeEnum.WITHDRAWAL) {
            if (bankAccount.getBalance() < transaction.getAmount()) {
                return ResponseUtil.getResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient balance", null);
            }

            bankAccount.setBalance(bankAccount.getBalance() - transaction.getAmount());
        }

        transaction.setDate(LocalDate.now());

        bankAccountRepository.save(bankAccount);
        transactionRepository.save(transaction);

        return ResponseUtil.getResponse(HttpStatus.OK.value(), "Transaction done", null);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAccountTransactions(String accountId) {
        List<Transaction> transactionList = transactionRepository.findAllByAccountId(accountId).stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());

        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of transactions", transactionList);
    }
}
