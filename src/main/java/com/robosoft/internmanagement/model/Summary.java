package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Summary
{
    private int applications;
    private int shortlisted;
    private int onHold;
    private int rejected;
}
