package com.ms.client.controller;

import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.service.impl.ClientServiceImpl;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientServiceImpl clientApiDelegate;

    public ClientController(ClientServiceImpl clientApiDelegate) {
        this.clientApiDelegate = clientApiDelegate;
    }

    @PostMapping
    public Mono<ResponseEntity<ModelApiResponse>> createClient(@RequestBody Client client) {
        logger.info("Request to create client: {}", client.getName());
        return clientApiDelegate.createClient(client);
    }

    @DeleteMapping("/{clientId}")
    public Mono<ResponseEntity<Void>> deleteClient(@PathVariable String clientId) {
        logger.info("Request to delete client with ID: {}", clientId);
        return clientApiDelegate.deleteClient(clientId);
    }

    @GetMapping("/{clientId}")
    public Mono<ResponseEntity<ModelApiResponse>> getClientById(@PathVariable String clientId) {
        logger.info("Request to fetch client with ID: {}", clientId);
        return clientApiDelegate.getClientById(clientId);
    }

    @GetMapping
    public Mono<ResponseEntity<ModelApiResponse>> getAllClients() {
        logger.info("Request to fetch all clients");
        return clientApiDelegate.getAllClients();
    }

    @PutMapping("/{clientId}")
    public Mono<ResponseEntity<Void>> updateClient(@PathVariable String clientId, @RequestBody Client client) {
        logger.info("Request to update client with ID: {}", clientId);
        return clientApiDelegate.updateClient(clientId, client);
    }
}
