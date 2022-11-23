package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Application {

    private int candidateId;
    private String imageUrl;
    private String emailId;
    private long mobileNumber;
    private String designation;
    private String location;
    private Date date;

}
