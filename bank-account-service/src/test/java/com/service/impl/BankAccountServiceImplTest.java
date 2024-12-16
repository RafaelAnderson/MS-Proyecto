package com.service.impl;

import com.ms.bank.account.model.BankAccount;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.BankAccountRepository;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.client.ClientServiceFeignClient;
import com.ms.bank.account.service.impl.BankAccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

class BankAccountServiceImplTest {

    @Mock
    private ClientServiceFeignClient clientServiceFeignClient;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditRepository creditRepository;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldReturnConflict_whenAccountAlreadyExists() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setClientId("832tex329876");
        bankAccount.setType(BankAccount.TypeEnum.CURRENT);

        when(bankAccountRepository.findByClientId(bankAccount.getClientId()))
                .thenReturn(List.of(bankAccount));

        ResponseEntity<ModelApiResponse> response = bankAccountService.createAccount(bankAccount);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("The client already has an account"));
    }

    @Test
    void deleteAccount_shouldReturnNotFound_whenAccountDoesNotExist() {
        String accountId = "432121fd32f";

        when(bankAccountRepository.existsById(accountId)).thenReturn(false);

        ResponseEntity<Void> response = bankAccountService.deleteAccount(accountId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteAccount_shouldReturnOk_whenAccountDeleted() {
        String accountId = "432121fd32f";

        when(bankAccountRepository.existsById(accountId)).thenReturn(true);

        ResponseEntity<Void> response = bankAccountService.deleteAccount(accountId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
