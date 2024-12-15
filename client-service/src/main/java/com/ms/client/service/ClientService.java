package com.ms.client.service;

import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ClientService {
    Mono<ResponseEntity<ModelApiResponse>> createClient(Client client);

    Mono<ResponseEntity<Void>> deleteClient(String clientId);

    Mono<ResponseEntity<ModelApiResponse>> getClientById(String clientId);

    Mono<ResponseEntity<ModelApiResponse>> getAllClients();

    Mono<ResponseEntity<Void>> updateClient(String clientId, Client client);
}
