package com.robosoft.internmanagement.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DatabaseException extends RuntimeException {
    Result result;

    public DatabaseException(Result result) {
        super(result.getDescription());
        this.result = result;
    }
}
