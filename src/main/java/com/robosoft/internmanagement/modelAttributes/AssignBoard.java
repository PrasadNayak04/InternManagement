package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AssignBoard
{

    private int candidateId;
    private String recruiterEmail;
    private String organizerEmail;
    private Date assignDate;
    private Date interviewDate;
    private String status;
    private boolean okWithOtherLocations;


}
