package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CandidateInvite
{

    private int candidateInviteId;
    @NotBlank
    @Pattern(regexp = "[a-zA-Z( )]+")
    private String candidateName;
    @NotBlank
    private String designation;
    @Min(value = 1000000000L)
    @Max(value = 9999999999L)
    private long mobileNumber;
    @NotBlank
    private String location;
    private String jobDetails;
    @NotBlank
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.(com|in|org)")
    private String candidateEmail;

}
