package com.yulink.texas.common.model;

import lombok.Data;

@Data
public class EmptyResponse {

    private boolean status;

    private EmptyResponse() {
    }

    public static EmptyResponse create() {
        return new EmptyResponse();
    }

    public static EmptyResponse create(boolean status) {
        EmptyResponse response = create();
        response.setStatus(status);
        return response;
    }
}
