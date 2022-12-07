package com.robosoft.internmanagement.model;

import com.robosoft.internmanagement.exception.Result;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseData<T> {

    T info;
    Result result;

}
