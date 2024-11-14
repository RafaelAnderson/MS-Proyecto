package com.ms.client.impl;

import com.ms.client.api.ClientsApiDelegate;
import com.ms.client.model.Client;
import com.ms.client.model.ModelApiResponse;
import com.ms.client.repository.ClientRepository;
import com.ms.client.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientApiDelegateImpl implements ClientsApiDelegate {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public  ResponseEntity<ModelApiResponse> createClient(Client client) {
        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), "Account created successfully", clientRepository.save(client));
    }

    @Override
    public ResponseEntity<Void> deleteClient(String clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if (clientOptional.isPresent()) {
            clientRepository.deleteById(clientId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public  ResponseEntity<ModelApiResponse> getClientById(String clientId) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        return clientOptional
                .map(client -> ResponseUtil.getResponse(HttpStatus.OK.value(), "Client", client))
                .orElseGet(() -> ResponseUtil.getResponse(HttpStatus.NOT_FOUND.value(), "Client", null));
    }

    @Override
    public  ResponseEntity<ModelApiResponse> getAllClients() {
        List<Client> clients = clientRepository.findAll();

        return ResponseUtil.getResponse(HttpStatus.OK.value(), "List of clients", clients);
    }

    @Override
    public ResponseEntity<Void> updateClient(String clientId, Client client) {
        Optional<Client> clientOptional = clientRepository.findById(clientId);

        if(clientOptional.isPresent()) {
            Client existingClient = clientOptional.get();

            existingClient.setPhone(client.getPhone());
            existingClient.setAddress(client.getAddress());

            clientRepository.save(existingClient);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }
}
