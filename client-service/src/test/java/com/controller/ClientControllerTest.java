package com.controller;

import com.ms.client.controller.ClientController;
import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.service.impl.ClientServiceImpl;
import com.mock.ClientMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class ClientControllerTest {

    @MockBean
    private ClientServiceImpl clientService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new ClientController(clientService)).build();
    }

    @Test
    void testCreateClient_Success() {
        ModelApiResponse response = new ModelApiResponse(HttpStatus.CREATED.value(), "Client created successfully", null);

        given(clientService.createClient(ClientMock.client_personal_pyme_dto()))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(response)));

        webTestClient.post()
                .uri("/api/clients")
                .bodyValue(ClientMock.client_personal_pyme_dto())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ModelApiResponse.class)
                .isEqualTo(response);
    }


    @Test
    void testGetClientById_Success() {
        String clientId = "h6732hc4f987hg";
        Client client = ClientMock.client_personal_vip_entity();
        ModelApiResponse expectedResponse = new ModelApiResponse(HttpStatus.OK.value(), "Client found", client);

        given(clientService.getClientById(clientId)).willReturn(Mono.just(ResponseEntity.status(HttpStatus.OK).body(expectedResponse)));

        webTestClient.get()
                .uri("/api/clients/{clientId}", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("Client found");
    }

    @Test
    void testGetClientById_ClientNotFound() {
        String clientId = "12345";
        ModelApiResponse response = new ModelApiResponse(HttpStatus.NOT_FOUND.value(), "Client not found", null);

        given(clientService.getClientById(clientId)).willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)));

        webTestClient.get()
                .uri("/api/clients/{clientId}", clientId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ModelApiResponse.class)
                .isEqualTo(response);
    }

    @Test
    void testGetAllClients_Success() {
        ModelApiResponse response = new ModelApiResponse(HttpStatus.OK.value(), "Clients fetched successfully", null);

        given(clientService.getAllClients()).willReturn(Mono.just(ResponseEntity.status(HttpStatus.OK).body(response)));

        webTestClient.get()
                .uri("/api/clients")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ModelApiResponse.class)
                .isEqualTo(response);
    }

    @Test
    void testCreateClient_ClientExists() {
        ModelApiResponse response = new ModelApiResponse(HttpStatus.CONFLICT.value(), "Client already exists", null);

        given(clientService.createClient(ClientMock.client_personal_vip_entity()))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response)));

        webTestClient.post()
                .uri("/api/clients")
                .bodyValue(ClientMock.client_personal_vip_entity())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ModelApiResponse.class)
                .isEqualTo(response);
    }

    @Test
    void testDeleteClient_Success() {
        String clientId = "h6732hc4f987hg";

        given(clientService.deleteClient(clientId)).willReturn(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));

        webTestClient.delete()
                .uri("/api/clients/{clientId}", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }


    @Test
    void testDeleteClient_ClientNotFound() {
        String clientId = "12345";

        given(clientService.deleteClient(clientId)).willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

        webTestClient.delete()
                .uri("/api/clients/{clientId}", clientId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
    }

    @Test
    void testUpdateClient_Success() {
        String clientId = "h6732hc4f987hg";
        Client client = new Client();

        given(clientService.updateClient(clientId, client))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.OK).build()));

        webTestClient.put()
                .uri("/api/clients/{clientId}", clientId)
                .bodyValue(client)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    void testUpdateClient_ClientNotFound() {
        String clientId = "12345";
        Client client = new Client();

        given(clientService.updateClient(clientId, client))
                .willReturn(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

        webTestClient.put()
                .uri("/api/clients/{clientId}", clientId)
                .bodyValue(client)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
    }

}
