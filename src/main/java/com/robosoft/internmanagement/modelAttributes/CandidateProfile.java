package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CandidateProfile {

    @NotBlank
    @Pattern(regexp = "[a-zA-Z( )]+")
    private String name;
    private Date dob;
    @Min(value = 1000000000L)
    @Max(value = 9999999999L)
    private long mobileNumber;
    @NotBlank
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.(com|in|org)")
    private String emailId;
    private Date date;
    @NotBlank
    private String jobLocation;
    @NotBlank
    private String gender;
    @NotBlank
    private String position;
    private int expYear;
    private int expMonth;
    @NotBlank
    private String candidateType;
    private String contactPerson;
    private String languagesKnown;
    private List<WorkHistory> workHistories;
    @NotEmpty
    private List<Education> educations;
    @Valid
    private Address address;
    private String softwareWorked;
    @NotBlank
    private String skills;
    private String about;
    private List<Link> links;
    private double currentCTC;
    private double expectedCTC;
    private MultipartFile attachment;
    private MultipartFile photo;

}
