package impl;

import com.ms.client.impl.ClientApiDelegateImpl;
import com.ms.client.model.ModelApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ms.client.model.Client;
import com.ms.client.repository.ClientRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


class ClientApiDelegateImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientApiDelegateImpl clientApiDelegate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateClient_whenClientExists() {
        Client client = new Client();
        client.setDocument("12345");

        when(clientRepository.existsByDocument("12345")).thenReturn(true);

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.createClient(client);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("The client is already registered", response.getBody().getMessage());
    }

    @Test
    void testCreateClient_whenClientDoesNotExist() {
        Client client = new Client();
        client.setDocument("12345");
        client.setProfile(Client.ProfileEnum.PYME);
        client.setType(Client.TypeEnum.BUSINESS);

        when(clientRepository.existsByDocument("12345")).thenReturn(false);
        when(clientRepository.save(client)).thenReturn(client);

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.createClient(client);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Account created successfully", response.getBody().getMessage());
    }

    @Test
    void testCreateClient_whenProfileIsIncorrect() {
        Client client = new Client();
        client.setDocument("12345");
        client.setType(Client.TypeEnum.PERSONAL);
        client.setProfile(Client.ProfileEnum.PYME);

        when(clientRepository.existsByDocument("12345")).thenReturn(false);

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.createClient(client);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("The client doesn't have the correct profile " + client.getProfile(), response.getBody().getMessage());
    }

    @Test
    void testDeleteClient_whenClientExists() {
        String clientId = "12345";
        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ResponseEntity<Void> response = clientApiDelegate.deleteClient(clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteClient_whenClientDoesNotExist() {
        String clientId = "12345";

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = clientApiDelegate.deleteClient(clientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetClientById_whenClientExists() {
        String clientId = "12345";
        Client client = new Client();
        client.setId(clientId);
        client.setName("John Doe");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.getClientById(clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Client", response.getBody().getMessage());
    }

    @Test
    void testGetClientById_whenClientDoesNotExist() {
        String clientId = "12345";

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.getClientById(clientId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Client", response.getBody().getMessage());
    }

    @Test
    void testGetAllClients_whenClientsExist() {
        Client client1 = new Client();
        client1.setId("12345");
        Client client2 = new Client();
        client2.setId("67890");

        when(clientRepository.findAll()).thenReturn(List.of(client1, client2));

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.getAllClients();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("List of clients", response.getBody().getMessage());
        assertEquals(2, ((List<Client>) response.getBody().getData()).size());
    }

    @Test
    void testGetAllClients_whenNoClientsExist() {
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<ModelApiResponse> response = clientApiDelegate.getAllClients();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("List of clients", response.getBody().getMessage());
        assertEquals(0, ((List<Client>) response.getBody().getData()).size());
    }

    @Test
    void testUpdateClient_whenClientExists() {
        String clientId = "12345";
        Client client = new Client();
        client.setId(clientId);
        client.setPhone("123-456-7890");
        client.setAddress("123 Street");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        ResponseEntity<Void> response = clientApiDelegate.updateClient(clientId, client);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateClient_whenClientDoesNotExist() {
        String clientId = "12345";
        Client client = new Client();
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = clientApiDelegate.updateClient(clientId, client);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
