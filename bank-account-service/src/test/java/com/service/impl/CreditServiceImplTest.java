package com.service.impl;

import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.repository.CreditRepository;
import com.ms.bank.account.service.impl.CreditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
class CreditServiceImplTest {

    @Mock
    private CreditRepository creditRepository;

    @InjectMocks
    private CreditServiceImpl creditService;

    private Credit credit;

    @BeforeEach
    void setUp() {
        credit = new Credit();
        credit.setId("1");
        credit.setClientId("client1");
        credit.setType(Credit.TypeEnum.PERSONAL);
        reset(creditRepository);
    }

    @Test
    void testCreateCredit_Conflict() {
        when(creditRepository.existsByClientIdAndType("client1", Credit.TypeEnum.PERSONAL)).thenReturn(true);

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CONFLICT)
                .verifyComplete();
    }

    @Test
    void testCreateCredit_Success() {
        when(creditRepository.existsByClientIdAndType("client1", Credit.TypeEnum.PERSONAL)).thenReturn(false);
        when(creditRepository.save(credit)).thenReturn(credit);

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.createCredit(credit);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void testDeleteCredit_ClientExists() {
        String creditId = "1";
        when(creditRepository.existsById(creditId)).thenReturn(true);
        doNothing().when(creditRepository).deleteById(creditId);

        Mono<ResponseEntity<Void>> result = creditService.deleteCredit(creditId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testDeleteCredit_ClientNotFound() {
        String creditId = "1";
        when(creditRepository.existsById(creditId)).thenReturn(false);

        Mono<ResponseEntity<Void>> result = creditService.deleteCredit(creditId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void testGetCreditById_ClientExists() {
        String creditId = "1";
        when(creditRepository.findById(creditId)).thenReturn(Optional.of(credit));

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.getCreditById(creditId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testGetCreditById_ClientNotFound() {
        String creditId = "1";
        when(creditRepository.findById(creditId)).thenReturn(Optional.empty());

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.getCreditById(creditId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void testGetAllCredits_Success() {
        when(creditRepository.findAll()).thenReturn(java.util.List.of(credit));

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.getAllCredits();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testGetCreditsByClientId_Exists() {
        String clientId = "client1";
        when(creditRepository.findByClientId(clientId)).thenReturn(java.util.List.of(credit));

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.getCreditsByClientId(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testGetCreditsByClientId_NotFound() {
        String clientId = "client1";
        when(creditRepository.findByClientId(clientId)).thenReturn(java.util.Collections.emptyList());

        Mono<ResponseEntity<ModelApiResponse>> result = creditService.getCreditsByClientId(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }
}
