package com.ms.bank.account.service.impl;

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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionsServiceImpl implements TransactionsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsServiceImpl.class);

    private final BankAccountRepository bankAccountRepository;
    private final CreditRepository creditRepository;
    private final TransactionRepository transactionRepository;

    public TransactionsServiceImpl(BankAccountRepository bankAccountRepository,
                                   CreditRepository creditRepository,
                                   TransactionRepository transactionRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.creditRepository = creditRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<ModelApiResponse> createTransaction(Transaction transaction) {
        ResponseEntity<ModelApiResponse> validationResponse = validTransaction(transaction);
        if (validationResponse != null) {
            return validationResponse;
        }

        String accountId = transaction.getAccountId();

        Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(accountId);

        if (bankAccountOptional.isPresent()) {
            logger.info("BankAccount found with ID: {}", accountId);
            return processBankAccountTransaction(transaction, bankAccountOptional.get());
        }

        Optional<Credit> creditOptional = creditRepository.findById(accountId);

        if (creditOptional.isPresent()) {
            logger.info("Credit found with ID: {}", accountId);
            return processCreditTransaction(transaction, creditOptional.get());
        }

        logger.error("No BankAccount or Credit found for ID: {}", accountId);
        return ResponseUtil.getResponse(HttpStatus.NO_CONTENT.value(), "Error getting account", null);
    }

    private ResponseEntity<ModelApiResponse> validTransaction(Transaction transaction) {
        if (Objects.isNull(transaction.getAccountId()) || transaction.getAccountId().isBlank()) {
            return ResponseUtil.buildBadRequestResponse("Invalid account");
        }

        if (transaction.getAmount() <= 0) {
            return ResponseUtil.buildBadRequestResponse("Invalid amount");
        }

        if (Objects.isNull(transaction.getType())) {
            return ResponseUtil.buildBadRequestResponse("Invalid type operation");
        }

        if (Objects.isNull(transaction.getDescription()) || transaction.getDescription().isBlank()) {
            return ResponseUtil.buildBadRequestResponse("Description is required");
        }

        return null;
    }


    private ResponseEntity<ModelApiResponse> processBankAccountTransaction(Transaction transaction, BankAccount bankAccount) {
        if (bankAccount == null) {
            return ResponseUtil.buildBadRequestResponse("Bank account not found");
        }

        switch (transaction.getType()) {
            case DEPOSIT:
                bankAccount.setBalance(bankAccount.getBalance() + transaction.getAmount());
                logger.info("Deposited {} to BankAccount with ID: {}", transaction.getAmount(), bankAccount.getId());
                break;
            case WITHDRAWAL:
                if (bankAccount.getBalance() < transaction.getAmount()) {
                    return ResponseUtil.buildBadRequestResponse("Insufficient balance for withdrawal");
                }
                bankAccount.setBalance(bankAccount.getBalance() - transaction.getAmount());
                logger.info("Withdrew {} from BankAccount with ID: {}", transaction.getAmount(), bankAccount.getId());
                break;
            case PURCHASE:
                if (bankAccount.getBalance() < transaction.getAmount()) {
                    return ResponseUtil.buildBadRequestResponse("Insufficient balance for purchase");
                }
                bankAccount.setBalance(bankAccount.getBalance() - transaction.getAmount());
                logger.info("Purchased with {} from BankAccount with ID: {}", transaction.getAmount(), bankAccount.getId());
                break;
            default:
                logger.error("Invalid transaction type");
                return ResponseUtil.buildBadRequestResponse("Invalid transaction type");
        }

        transaction.setDate(LocalDate.now());
        transactionRepository.save(transaction);
        bankAccountRepository.save(bankAccount);

        return ResponseUtil.getResponse(HttpStatus.OK.value(), "Transaction processed successfully for BankAccount", null);
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

