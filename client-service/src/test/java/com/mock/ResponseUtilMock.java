package com.mock;

import com.ms.client.model.ModelApiResponse;

public class ResponseUtilMock {

    public static ModelApiResponse getResponse(Integer status, String message, Object data) {
        return ModelApiResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}
