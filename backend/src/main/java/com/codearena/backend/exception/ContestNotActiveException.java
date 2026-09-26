package com.codearena.backend.exception;

import org.springframework.http.HttpStatus;

public class ContestNotActiveException extends ApiException {

    public ContestNotActiveException() {
        super(HttpStatus.FORBIDDEN, "This contest is not currently active");
    }
}
