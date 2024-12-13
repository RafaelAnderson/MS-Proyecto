package com.ms.bank.account.client;

import com.ms.bank.account.model.ModelApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "client-service", url = "http://localhost:8080/api")
public interface ClientServiceFeignClient {

    @GetMapping("/clients/{clientId}")
    ModelApiResponse getClientById(@PathVariable("clientId") String clientId);
}