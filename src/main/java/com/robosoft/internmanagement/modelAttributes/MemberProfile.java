package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class MemberProfile {

    private String name;
    private String emailId;
    private long mobileNumber;
    private MultipartFile photo;
    private String designation;
    private String position;
    private String password;

}
