package com.example.dropshop.domain.drops.exception;

import lombok.Getter;

@Getter
public class DropsException extends RuntimeException {

    private final DropsErrorCode errorCode;

    public DropsException(DropsErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

