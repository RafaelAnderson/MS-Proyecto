package com.ms.bank.account.util;

import com.ms.bank.account.model.ModelApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseUtil {

    public static ResponseEntity<ModelApiResponse> getResponse(Integer status, String message, Object data) {
        ModelApiResponse response = ModelApiResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.valueOf(status));
    }

    public static ResponseEntity<ModelApiResponse> getMapResponse(Integer status, String message, Map<String, Object> data) {
        ModelApiResponse response = ModelApiResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();

        return new ResponseEntity<>(response, HttpStatus.valueOf(status));
    }

    public static ResponseEntity<ModelApiResponse> buildConflictResponse(String message) {
        return ResponseUtil.getResponse(HttpStatus.CONFLICT.value(), message, null);
    }

    public static ResponseEntity<ModelApiResponse> buildBadRequestResponse(String message) {
        return ResponseUtil.getResponse(HttpStatus.BAD_REQUEST.value(), message, null);
    }

    public static ResponseEntity<ModelApiResponse> buildCreatedResponse(String message, Object object) {
        return ResponseUtil.getResponse(HttpStatus.CREATED.value(), message, object);
    }
}
