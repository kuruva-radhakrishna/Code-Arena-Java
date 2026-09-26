package com.codearena.backend.exception;

import org.springframework.http.HttpStatus;

public class ContestNotLiveException extends ApiException {

    public ContestNotLiveException() {
        super(HttpStatus.FORBIDDEN, "This contest is not live yet");
    }
}
