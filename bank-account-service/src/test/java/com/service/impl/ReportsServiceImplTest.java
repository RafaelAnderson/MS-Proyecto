package com.service.impl;

import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.service.impl.ReportsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;

class ReportsServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditRepository creditRepository;

    @InjectMocks
    private ReportsServiceImpl reportsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(bankAccountRepository);
        reset(creditRepository);
    }

    @Test
    void generateProductReport_shouldReturnReport_whenClientHasActiveAccounts() {
        String clientId = "2739187dhg28";
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(100.0);
        Credit credit = new Credit();
        credit.setStatus(Credit.StatusEnum.ACTIVE);

        when(bankAccountRepository.findByClientId(clientId)).thenReturn(Arrays.asList(bankAccount));
        when(creditRepository.findByClientId(clientId)).thenReturn(Arrays.asList(credit));

        ResponseEntity<ModelApiResponse> response = reportsService.generateProductReport(clientId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

        Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
        assertTrue(responseData.containsKey("debitAccounts"));
        assertTrue(responseData.containsKey("creditAccounts"));

        verify(bankAccountRepository, times(1)).findByClientId(clientId);
        verify(creditRepository, times(1)).findByClientId(clientId);
    }

    @Test
    void generateProductReport_shouldReturnNotFound_whenNoAccountsFound() {
        String clientId = "2739187dhg28";

        when(bankAccountRepository.findByClientId(clientId)).thenReturn(Collections.emptyList());
        when(creditRepository.findByClientId(clientId)).thenReturn(Collections.emptyList());

        ResponseEntity<ModelApiResponse> response = reportsService.generateProductReport(clientId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

        verify(bankAccountRepository, times(1)).findByClientId(clientId);
        verify(creditRepository, times(1)).findByClientId(clientId);
    }

    @Test
    void generateProductReport_shouldReturnActiveProducts_01() {
        String clientId = "2739187dhg28";
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(1000.0);
        Credit credit = new Credit();
        credit.setStatus(Credit.StatusEnum.CLOSED);

        when(bankAccountRepository.findByClientId(clientId)).thenReturn(List.of(bankAccount));
        when(creditRepository.findByClientId(clientId)).thenReturn(List.of(credit));

        ResponseEntity<ModelApiResponse> response = reportsService.generateProductReport(clientId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

        Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();

        assertTrue(responseData.containsKey("debitAccounts"));
        assertTrue(responseData.containsKey("creditAccounts"));

        assertFalse(((Iterable) responseData.get("creditAccounts")).iterator().hasNext());
    }

    @Test
    void generateProductReport_shouldReturnActiveProducts_02() {
        String clientId = "2739187dhg28";
        BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(1000.0);
        Credit credit = new Credit();
        credit.setStatus(Credit.StatusEnum.ACTIVE);

        when(bankAccountRepository.findByClientId(clientId)).thenReturn(Arrays.asList(bankAccount));
        when(creditRepository.findByClientId(clientId)).thenReturn(Arrays.asList(credit));

        ResponseEntity<ModelApiResponse> response = reportsService.generateProductReport(clientId);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

        Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();

        assertTrue(responseData.containsKey("debitAccounts"));
        assertTrue(responseData.containsKey("creditAccounts"));

        assertTrue(((Iterable) responseData.get("creditAccounts")).iterator().hasNext());
    }
}
