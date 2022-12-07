package com.robosoft.internmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class Application {

    private int candidateId;
    private String emailId;
    private String name;
    private String imageUrl;
    private long mobileNumber;
    private String designation;
    private String location;
    private Date date;

}
