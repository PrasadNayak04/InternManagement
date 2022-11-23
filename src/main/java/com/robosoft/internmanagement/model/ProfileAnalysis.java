package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ProfileAnalysis {

    private int candidateId;
    private String name;
    private String imageUrl;
    private String skills;
    private String position;

}
