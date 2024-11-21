package com.ms.client.util;

import com.ms.client.model.ModelApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static ResponseEntity<ModelApiResponse> getResponse(Integer status, String message,
                                                               Object data) {
        ModelApiResponse response = ModelApiResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.valueOf(status));
    }
}
