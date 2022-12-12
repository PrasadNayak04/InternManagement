package com.robosoft.internmanagement.model;

import com.robosoft.internmanagement.exception.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ResponseData<T> {

    T info;
    Result result;

}
