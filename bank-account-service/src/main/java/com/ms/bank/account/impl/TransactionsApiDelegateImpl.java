package com.ms.bank.account.impl;

import com.ms.bank.account.api.TransactionsApiDelegate;
import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.model.Transaction;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.repository.TransactionRepository;
import com.ms.bank.account.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionsApiDelegateImpl implements TransactionsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsApiDelegateImpl.class);

    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;
    private final TransactionRepository transactionRepository;

    public TransactionsApiDelegateImpl(BankAccountRepository bankAccountRepository,
                                       CreditRepository creditRepository,
                                       TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.creditRepository = creditRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<ModelApiResponse> createTransaction(Transaction transaction) {
        validTransaction(transaction);

        String accountId = transaction.getAccountId();

        Mono<ResponseEntity<ModelApiResponse>> bankAccountMono = Mono.justOrEmpty(bankAccountRepository.findById(accountId))
                .map(bankAccount -> {
                    logger.info("BankAccount found with ID: {}", accountId);
                    return processBankAccountTransaction(transaction, bankAccount);
                });

        Mono<ResponseEntity<ModelApiResponse>> creditMono = Mono.justOrEmpty(creditRepository.findById(accountId))
                .map(credit -> {
                    logger.info("Credit found with ID: {}", accountId);
                    return processCreditTransaction(transaction, credit);
                });

        return Mono.firstWithValue(bankAccountMono, creditMono)
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("No BankAccount or Credit found for ID: {}", accountId);
                    return Mono.just(ResponseUtil.getResponse(HttpStatus.NO_CONTENT.value(), "Error getting account", null));
                })).block();
    }

    private void validTransaction(Transaction transaction) {
        if (Objects.isNull(transaction.getAccountId()) || transaction.getAccountId().isBlank()) {
            ResponseUtil.buildBadRequestResponse("Invalid account");
        }

        if (transaction.getAmount() <= 0) {
            ResponseUtil.buildBadRequestResponse("Invalid amount");
        }

        if (Objects.isNull(transaction.getType())) {
            ResponseUtil.buildBadRequestResponse("Invalid type operation");
        }

        if (Objects.isNull(transaction.getDescription()) || transaction.getDescription().isBlank()) {
            ResponseUtil.buildBadRequestResponse("Description is required");
        }
    }

    private ResponseEntity<ModelApiResponse> processBankAccountTransaction(Transaction transaction, BankAccount bankAccount) {
        return Mono.just(ResponseUtil.getResponse(HttpStatus.OK.value(), "Transaction processed successfully for BankAccount", null)).block();
    }

    private ResponseEntity<ModelApiResponse> processCreditTransaction(Transaction transaction, Credit credit) {
        Double amount = (transaction.getAmount() * (100 + credit.getInterestRate()) / 100);

        if (credit.getBalance() < amount) {
            return ResponseUtil.buildBadRequestResponse("The balance is insufficient to complete the operation");
        }

        transaction.setDate(LocalDate.now());
        credit.setBalance(credit.getBalance() - amount);

        transactionRepository.save(transaction);
        creditRepository.save(credit);
        return Mono.just(ResponseUtil.getResponse(HttpStatus.OK.value(), "Transaction processed successfully for Credit", null)).block();
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

