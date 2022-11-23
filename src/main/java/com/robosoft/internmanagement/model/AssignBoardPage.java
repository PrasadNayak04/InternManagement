package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AssignBoardPage
{

    private int candidateId;
    private String name;
    private String designation;
    private String location;
    private Date assignDate;
    private String organizer;

}
