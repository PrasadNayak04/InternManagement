package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LoggedProfile
{
    private String name;
    private String position;
    private String designation;
    @Min(value = 1000000000L)
    @Max(value = 9999999999L)
    private long mobileNumber;
    private String profileImage;
}
