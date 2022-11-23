package com.robosoft.internmanagement.modelAttributes;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CandidateProfile {

    private String name;
    private Date dob;
    private long mobileNumber;
    private String emailId;
    private Date date;
    private String jobLocation;
    private String gender;
    private String position;
    private int expYear;
    private int expMonth;
    private String candidateType;
    private String contactPerson;
    private String languagesKnown;
    private List<WorkHistory> workHistories;
    private List<Education> educations;
    private Address address;
    private String softwareWorked;
    private String skills;
    private String about;
    private List<Link> links;
    private double currentCTC;
    private double expectedCTC;
    private MultipartFile attachment;
    private MultipartFile photo;

}
