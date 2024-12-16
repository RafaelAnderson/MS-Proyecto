package com.ms.client.service.impl;

import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.repository.ClientRepository;
import com.ms.client.service.ClientService;
import com.ms.client.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Mono<ResponseEntity<ModelApiResponse>> createClient(Client client) {
        return existClient(client)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        logger.warn("Client with ID: {} is already registered", client.getId());
                        return Mono.just(ResponseUtil.getResponse(
                                HttpStatus.CONFLICT.value(), "The client is already registered", null));
                    }
                    return validClientProfile(client)
                            .flatMap(valid -> {
                                if (Boolean.FALSE.equals(valid)) {
                                    return Mono.just(ResponseUtil.getResponse(
                                            HttpStatus.CONFLICT.value(),
                                            "The client doesn't have the correct profile " + client.getProfile(), null));
                                }
                                return Mono.fromCallable(() -> clientRepository.save(client))
                                        .doOnSuccess(savedClient -> logger.debug("Client created with ID: {}", savedClient.getId()))
                                        .map(savedClient -> ResponseUtil.getResponse(HttpStatus.CREATED.value(),
                                                "Account created successfully", savedClient));
                            });
                });
    }

    private Mono<Boolean> existClient(Client client) {
        return Mono.fromCallable(() -> clientRepository.existsByDocument(client.getDocument()));
    }

    private Mono<Boolean> validClientProfile(Client client) {
        return Mono.just(client)
                .map(c -> c.getType().equals(Client.TypeEnum.PERSONAL) && c.getProfile().equals(Client.ProfileEnum.VIP) ||
                        c.getType().equals(Client.TypeEnum.BUSINESS) && c.getProfile().equals(Client.ProfileEnum.PYME));
    }

    public Mono<ResponseEntity<Void>> deleteClient(String clientId) {
        return Mono.fromCallable(() -> clientRepository.findById(clientId))
                .flatMap(clientOpt -> {
                    if (clientOpt.isPresent()) {
                        clientRepository.deleteById(clientId);
                        logger.info("Client with ID: {} deleted successfully", clientId);
                        return Mono.just(ResponseEntity.ok().build());
                    } else {
                        logger.warn("Client with ID: {} not found", clientId);
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }

    public Mono<ResponseEntity<ModelApiResponse>> getClientById(String clientId) {
        logger.info("Fetching client with ID: {}", clientId);

        Optional<Client> c = clientRepository.findById(clientId);

        return Mono.fromCallable(() -> clientRepository.findById(clientId))
                .flatMap(clientOpt -> {
                    if (clientOpt.isPresent()) {
                        Client client = clientOpt.get();
                        logger.debug("Client found: {}", client.getName());
                        return Mono.just(ResponseUtil.getResponse(HttpStatus.OK.value(), "Client", client));
                    } else {
                        return Mono.just(ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Client", null));
                    }
                });
    }


    public Mono<ResponseEntity<ModelApiResponse>> getAllClients() {
        return Mono.fromCallable(clientRepository::findAll)
                .flatMapIterable(clients -> clients)
                .collectList()
                .map(clients -> {
                    logger.debug("Total clients: {}", clients.size());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of clients", clients);
                });
    }

    public Mono<ResponseEntity<Void>> updateClient(String clientId, Client client) {
        return Mono.fromCallable(() -> clientRepository.findById(clientId))
                .flatMap(clientOpt -> {
                    if (clientOpt.isPresent()) {
                        Client existingClient = clientOpt.get();
                        existingClient.setPhone(client.getPhone());
                        existingClient.setAddress(client.getAddress());
                        return Mono.fromCallable(() -> clientRepository.save(existingClient))
                                .doOnSuccess(updatedClient -> logger.info("Client with ID: {} updated successfully", clientId))
                                .then(Mono.just(ResponseEntity.ok().build()));
                    } else {
                        logger.warn("Client with ID: {} not found for update", clientId);
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }
}
