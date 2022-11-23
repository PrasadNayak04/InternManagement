package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Address {

    private String content;
    private String state;
    private long pinCode;
    
}
