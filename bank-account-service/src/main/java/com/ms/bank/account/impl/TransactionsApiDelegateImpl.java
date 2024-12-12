package com.ms.bank.account.impl;

import com.ms.bank.account.api.TransactionsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.model.Transaction;
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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionsApiDelegateImpl implements TransactionsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsApiDelegateImpl.class);

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional
    public ResponseEntity<ModelApiResponse> createTransaction(Transaction transaction) {

        if (!bankAccountRepository.existsById(transaction.getAccountId())) {
            logger.warn("Bank account with ID: {} not found", transaction.getAccountId());
            return ResponseUtil.getResponse(HttpStatus.NO_CONTENT.value(), "Error getting account", null);
        }

        BankAccount bankAccount = bankAccountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (transaction.getAmount() <= 0) {
            logger.error("Invalid transaction amount: {}", transaction.getAmount());
            return ResponseUtil.getResponse(HttpStatus.BAD_REQUEST.value(), "Invalid amount", null);
        }

        if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
            bankAccount.setBalance(bankAccount.getBalance() + transaction.getAmount());
            logger.info("Deposit of {} made to account ID: {}. New balance: {}", transaction.getAmount(), transaction.getAccountId(), bankAccount.getBalance());
        } else if (transaction.getType() == Transaction.TypeEnum.WITHDRAWAL) {
            if (bankAccount.getBalance() < transaction.getAmount()) {
                logger.error("Insufficient balance for withdrawal of {} from account ID: {}", transaction.getAmount(), transaction.getAccountId());
                return ResponseUtil.getResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient balance", null);
            }

            bankAccount.setBalance(bankAccount.getBalance() - transaction.getAmount());
            logger.info("Withdrawal of {} made from account ID: {}. New balance: {}", transaction.getAmount(), transaction.getAccountId(), bankAccount.getBalance());
        }

        transaction.setDate(LocalDate.now());
        bankAccountRepository.save(bankAccount);
        transactionRepository.save(transaction);

        logger.info("Transaction completed successfully for account ID: {}", transaction.getAccountId());
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "Transaction done", null);
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAccountTransactions(String accountId) {
        logger.info("Request received to fetch transactions for account ID: {}", accountId);

        List<Transaction> transactionList = transactionRepository.findAllByAccountId(accountId).stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());

        logger.info("Found {} transactions for account ID: {}", transactionList.size(), accountId);
        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of transactions", transactionList);
    }
}

