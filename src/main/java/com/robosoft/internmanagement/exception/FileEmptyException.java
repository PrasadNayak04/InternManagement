package com.robosoft.internmanagement.exception;

import lombok.Data;

@Data
public class FileEmptyException extends RuntimeException{
    Result result;

    public FileEmptyException(Result result) {
        super(result.getDescription());
        this.result = result;
    }
}
