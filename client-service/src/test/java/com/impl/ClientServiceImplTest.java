package com.impl;

import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.repository.ClientRepository;
import com.ms.client.service.impl.ClientServiceImpl;
import com.mock.ClientMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @BeforeEach
    void setUp() {
        reset(clientRepository);
    }

    @Test
    void testCreateClient_ClientExists() {
        when(clientRepository.existsByDocument(ClientMock.client_personal_vip_dto().getDocument())).thenReturn(true);

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.createClient(ClientMock.client_personal_vip_dto());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CONFLICT)
                .verifyComplete();
    }

    @Test
    void testCreateClient_InvalidProfile_personal_pyme() {
        when(clientRepository.existsByDocument(ClientMock.client_personal_pyme_dto().getDocument())).thenReturn(false);

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.createClient(ClientMock.invalid_profile_personal_pyme());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CONFLICT)
                .verifyComplete();
    }

    @Test
    void testCreateClient_InvalidProfile_bussiness_vip() {
        when(clientRepository.existsByDocument(ClientMock.client_personal_pyme_dto().getDocument())).thenReturn(false);

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.createClient(ClientMock.invalid_profile_business_vip());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CONFLICT)
                .verifyComplete();
    }

    @Test
    void testCreateClient_Success_Personal() {
        when(clientRepository.existsByDocument(ClientMock.client_personal_vip_dto().getDocument())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(ClientMock.client_personal_vip_dto());

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.createClient(ClientMock.client_personal_vip_dto());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void testCreateClient_Success_Bussiness() {
        when(clientRepository.existsByDocument(ClientMock.client_personal_pyme_dto().getDocument())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(ClientMock.client_personal_pyme_dto());

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.createClient(ClientMock.client_personal_pyme_dto());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void testDeleteClient_ClientExists() {
        String clientId = "h6732hc4f987hg";
        given(clientRepository.findById(clientId)).willReturn(Optional.of(ClientMock.client_personal_vip_entity()));
        doNothing().when(clientRepository).deleteById(clientId);

        Mono<ResponseEntity<Void>> result = clientService.deleteClient(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testDeleteClient_ClientNotFound() {
        String clientId = "12345";
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Mono<ResponseEntity<Void>> result = clientService.deleteClient(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void testGetClientById_ClientExists() {
        String clientId = "h6732hc4f987hg";
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(ClientMock.client_personal_vip_entity()));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(ClientMock.client_personal_vip_entity()));

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.getClientById(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testGetClientById_ClientNotFound() {
        String clientId = "12345";
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.getClientById(clientId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    void testGetAllClients_Success() {
        when(clientRepository.findAll()).thenReturn(Collections.singletonList(ClientMock.client_personal_vip_entity()));

        Mono<ResponseEntity<ModelApiResponse>> result = clientService.getAllClients();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testUpdateClient_Success() {
        String clientId = "h6732hc4f987hg";
        Client clientToUpdate = ClientMock.client_personal_vip_entity();
        Client updatedClient = ClientMock.client_personal_vip_entity_2();

        given(clientRepository.findById(clientId)).willReturn(Optional.of(clientToUpdate));
        given(clientRepository.save(any(Client.class))).willReturn(updatedClient);

        Mono<ResponseEntity<Void>> result = clientService.updateClient(clientId, updatedClient);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    void testUpdateClient_ClientNotFound() {
        String clientId = "h6732hc4f987hg";
        Client updatedClient = ClientMock.client_personal_vip_entity();

        given(clientRepository.findById(clientId)).willReturn(Optional.empty());

        Mono<ResponseEntity<Void>> result = clientService.updateClient(clientId, updatedClient);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }
}
