package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CvAnalysis
{
    private String designation;
    private int applicants;
    private Date receivedDate;
    private String status;
    private List<String> locations;
}
