package com.controller;

import com.ms.bank.account.controller.ReportsController;
import com.ms.bank.account.model.ModelApiResponse;
import com.ms.bank.account.service.impl.ReportsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
class ReportsControllerTest {

    @InjectMocks
    private ReportsController reportsController;

    @Mock
    private ReportsServiceImpl reportsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportsController).build();
    }

    @Test
    void generateProductReport_shouldReturnOk() throws Exception {
        String clientId = "12345";
        ModelApiResponse response = new ModelApiResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("List of products for client " + clientId);

        when(reportsService.generateProductReport(clientId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/api/reports/generate-product-report/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("List of products for client " + clientId));

        verify(reportsService).generateProductReport(clientId);
    }
}
