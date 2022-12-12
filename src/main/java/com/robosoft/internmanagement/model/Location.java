package com.robosoft.internmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include. NON_NULL)
public class Location {

    private int locationId;
    private String location;
    private Integer vacancy;
}
