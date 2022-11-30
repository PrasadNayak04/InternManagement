package com.robosoft.internmanagement.exception;

import lombok.Data;

@Data
public class JwtTokenException extends RuntimeException{
    String message;
    public JwtTokenException(String message) {
        super(message);
        this.message = message;
    }
}
