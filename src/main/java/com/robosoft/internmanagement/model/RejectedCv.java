package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RejectedCv
{

    private int applicationId;
    private String name;
    private String imageUrl;
    private String designation;
    private String location;
    private long mobileNumber;

}
