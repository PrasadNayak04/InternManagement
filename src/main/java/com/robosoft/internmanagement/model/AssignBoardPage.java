package com.robosoft.internmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class AssignBoardPage
{

    private int candidateId;
    private String name;
    private String designation;
    private String location;
    private Date assignDate;
    private String organizer;
    private String status;

}
