package com.ms.client.impl;

import com.ms.client.api.ClientsApiDelegate;
import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.repository.ClientRepository;
import com.ms.client.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Transactional
public class ClientApiDelegateImpl implements ClientsApiDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ClientApiDelegateImpl.class);

    private final ClientRepository clientRepository;

    public ClientApiDelegateImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ResponseEntity<ModelApiResponse> createClient(Client client) {

        if (existClient(client)) {
            return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(),
                    "The client is already registered", null);
        }

        if (!validClientProfile(client)) {
            return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(),
                    "The client doesn't have the correct profile " + client.getProfile(), null);
        }

        Client savedClient = clientRepository.save(client);
        logger.debug("Client created with ID: {}", savedClient.getId());
        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), "Account created successfully", savedClient);
    }

    private boolean existClient(Client client) {
        return clientRepository.existsByDocument(client.getDocument());
    }

    private boolean validClientProfile(Client client) {
        return client.getType().equals(Client.TypeEnum.PERSONAL) && client.getProfile().equals(Client.ProfileEnum.VIP) ||
                client.getType().equals(Client.TypeEnum.BUSINESS) && client.getProfile().equals(Client.ProfileEnum.PYME);
    }

    @Override
    public ResponseEntity<Void> deleteClient(String clientId) {
        logger.info("Deleting client with ID: {}", clientId);
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if (clientOptional.isPresent()) {
            clientRepository.deleteById(clientId);
            logger.info("Client with ID: {} deleted successfully", clientId);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("Client with ID: {} not found", clientId);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<ModelApiResponse> getClientById(String clientId) {
        logger.info("Fetching client with ID: {}", clientId);

        return Mono.justOrEmpty(clientRepository.findById(clientId))
                .map(client -> {
                    logger.debug("Client found: {}", client.getName());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "Client", client);
                })
                .defaultIfEmpty(ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Client", null))
                .block();
    }

    @Override
    public ResponseEntity<ModelApiResponse> getAllClients() {

        return Flux.fromIterable(clientRepository.findAll())
                .collectList()
                .map(clients -> {
                    logger.debug("Total clients: {}", clients.size());
                    return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of clients", clients);
                })
                .block();
    }

    @Override
    public ResponseEntity<Void> updateClient(String clientId, Client client) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if (clientOptional.isPresent()) {
            Client existingClient = clientOptional.get();

            existingClient.setPhone(client.getPhone());
            existingClient.setAddress(client.getAddress());

            clientRepository.save(existingClient);
            logger.info("Client with ID: {} updated successfully", clientId);
            return ResponseEntity.ok().build();
        }

        logger.warn("Client with ID: {} not found for update", clientId);
        return ResponseEntity.notFound().build();
    }
}