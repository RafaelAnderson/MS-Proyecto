package com.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;

import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.model.Transaction;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.repository.TransactionRepository;
import com.ms.bank.account.service.impl.TransactionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

class TransactionServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionsServiceImpl transactionsApiDelegate;

    private Transaction transaction;
    private BankAccount bankAccount;
    private Credit credit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transaction = new Transaction();
        transaction.setAccountId("321321312");
        transaction.setAmount(100.0);
        transaction.setType(Transaction.TypeEnum.DEPOSIT);
        transaction.setDescription("Deposit to account");

        bankAccount = new BankAccount();
        bankAccount.setId("89372164987");
        bankAccount.setBalance(200.0);

        credit = new Credit();
        credit.setId("4324132421");
        credit.setBalance(1000.0);
        credit.setInterestRate(10.0);
    }

    @Test
    void createTransaction_withBankAccount_shouldProcessTransactionSuccessfully() {
        when(bankAccountRepository.findById("321321312")).thenReturn(Optional.of(bankAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.createTransaction(transaction);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("Transaction processed successfully for BankAccount", response.getBody().getMessage());
        verify(transactionRepository, times(1)).save(transaction);
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void createTransaction_withCredit_shouldProcessTransactionSuccessfully() {
        when(creditRepository.findById(anyString())).thenReturn(Optional.of(credit));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(creditRepository.save(any(Credit.class))).thenReturn(credit);

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.createTransaction(transaction);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("Transaction processed successfully for Credit", response.getBody().getMessage());
        verify(transactionRepository, times(1)).save(transaction);
        verify(creditRepository, times(1)).save(credit);
    }

    @Test
    void createTransaction_withInvalidAmount_shouldReturnBadRequest() {
        transaction.setAmount(-50.0);
        when(bankAccountRepository.findById("12345")).thenReturn(Optional.of(bankAccount));

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.createTransaction(transaction);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Invalid amount", response.getBody().getMessage());
    }

    @Test
    void createTransaction_withInsufficientBalance_shouldReturnBadRequest() {
        transaction.setAmount(300.0);
        transaction.setType(Transaction.TypeEnum.WITHDRAWAL);
        when(bankAccountRepository.findById("321321312")).thenReturn(Optional.of(bankAccount));

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.createTransaction(transaction);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals("Insufficient balance for withdrawal", response.getBody().getMessage());
    }

    @Test
    void createTransaction_withNoAccountOrCredit_shouldReturnNoContent() {
        when(bankAccountRepository.findById("12345")).thenReturn(Optional.empty());
        when(creditRepository.findById("12345")).thenReturn(Optional.empty());

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.createTransaction(transaction);

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCodeValue());
        assertEquals("Error getting account", response.getBody().getMessage());
    }

    @Test
    void getAccountTransactions_shouldReturnTransactions() {
        when(transactionRepository.findAllByAccountId("12345")).thenReturn(List.of(transaction));

        ResponseEntity<ModelApiResponse> response = transactionsApiDelegate.getAccountTransactions("12345");

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("List of transactions", response.getBody().getMessage());
        assertTrue(response.getBody().getData() instanceof List);
    }
}
