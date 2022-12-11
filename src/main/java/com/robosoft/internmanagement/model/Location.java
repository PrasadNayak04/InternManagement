package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Location {
    private String location;
    private Integer vacancy;
}
