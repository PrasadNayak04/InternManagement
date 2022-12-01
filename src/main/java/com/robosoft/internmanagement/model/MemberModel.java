package com.robosoft.internmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class MemberModel
{

    private String emailId;
    private String name;
    private String photoUrl;
    private Long mobileNumber;
    private String designation;
    private String position;
    private String token;

    //Register response
    public MemberModel(String emailId, String name, String photoUrl, Long mobileNumber, String designation, String position) {
        this.emailId = emailId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.mobileNumber = mobileNumber;
        this.designation = designation;
        this.position = position;
    }

}
