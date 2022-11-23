package com.robosoft.internmanagement.model;

import com.robosoft.internmanagement.modelAttributes.Education;
import com.robosoft.internmanagement.modelAttributes.Link;
import com.robosoft.internmanagement.modelAttributes.WorkHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ExtendedCV {

    private int candidateId;
    private String name;
    private Date dob;
    private long mobileNumber;
    private String emailId;
    private String jobLocation;
    private String position;
    private int expYear;
    private int expMonth;
    private String candidateType;
    private String contactPerson;
    private String languagesKnown;
    private List<WorkHistory> workHistories;
    private List<Education> educations;
    private String softwaresWorked;
    private String skills;
    private String about;
    private List<Link> links;
    private double expectedCTC;
    private String attachmentUrl;
    private String imageUrl;

}
