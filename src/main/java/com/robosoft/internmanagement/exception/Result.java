package com.robosoft.internmanagement.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Result {
    String value;
    String description;
    String opinion;
}
