package com.controller;

import com.ms.bank.account.controller.CreditController;
import com.ms.bank.account.model.Credit;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.service.CreditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class CreditControllerTest {

    @Mock
    private CreditService creditService;

    @InjectMocks
    private CreditController creditController;

    private WebTestClient webTestClient;

    private Credit credit;

    @BeforeEach
    void setUp() {
        credit = new Credit();
        credit.setId("1");
        credit.setClientId("client1");
        credit.setType(Credit.TypeEnum.PERSONAL);

        webTestClient = WebTestClient.bindToController(creditController).build();
    }

    @Test
    void testCreateCredit_Success() {
        given(creditService.createCredit(credit))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ModelApiResponse(HttpStatus.CREATED.value(), "Credit created", credit))));

        webTestClient.post()
                .uri("/api/credits")
                .bodyValue(credit)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.CREATED.value())
                .jsonPath("$.message").isEqualTo("Credit created");
    }

    @Test
    void testCreateCredit_Conflict() {
        given(creditService.createCredit(credit))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ModelApiResponse(HttpStatus.CONFLICT.value(), "Credit already exists", null))));

        webTestClient.post()
                .uri("/api/credits")
                .bodyValue(credit)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT.value())
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.CONFLICT.value())
                .jsonPath("$.message").isEqualTo("Credit already exists");
    }

    @Test
    void testDeleteCredit_Success() {
        String creditId = "1";
        given(creditService.deleteCredit(creditId))
                .willReturn(Mono.just(ResponseEntity.ok().build()));

        webTestClient.delete()
                .uri("/api/credits/{creditId}", creditId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteCredit_NotFound() {
        String creditId = "1";
        given(creditService.deleteCredit(creditId))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

        webTestClient.delete()
                .uri("/api/credits/{creditId}", creditId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetCreditById_Success() {
        String creditId = "1";
        given(creditService.getCreditById(creditId))
                .willReturn(Mono.just(ResponseEntity.ok().body(new ModelApiResponse(HttpStatus.OK.value(), "Credit found", credit))));

        webTestClient.get()
                .uri("/api/credits/{creditId}", creditId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Credit found");
    }

    @Test
    void testGetCreditById_NotFound() {
        String creditId = "1";
        given(creditService.getCreditById(creditId))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ModelApiResponse(HttpStatus.NOT_FOUND.value(), "Credit not found", null))));

        webTestClient.get()
                .uri("/api/credits/{creditId}", creditId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("Credit not found");
    }

    @Test
    void testGetAllCredits_Success() {
        given(creditService.getAllCredits())
                .willReturn(Mono.just(ResponseEntity.ok().body(new ModelApiResponse(HttpStatus.OK.value(), "Credits found", credit))));

        webTestClient.get()
                .uri("/api/credits")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Credits found");
    }

    @Test
    void testGetCreditsByClientId_Success() {
        String clientId = "client1";
        given(creditService.getCreditsByClientId(clientId))
                .willReturn(Mono.just(ResponseEntity.ok().body(new ModelApiResponse(HttpStatus.OK.value(), "Credits found for client", credit))));

        webTestClient.get()
                .uri("/api/credits/client/{clientId}", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Credits found for client");
    }

    @Test
    void testGetCreditsByClientId_NotFound() {
        String clientId = "client1";
        given(creditService.getCreditsByClientId(clientId))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ModelApiResponse(HttpStatus.NOT_FOUND.value(), "No credits found for client", null))));

        webTestClient.get()
                .uri("/api/credits/client/{clientId}", clientId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("No credits found for client");
    }
}
