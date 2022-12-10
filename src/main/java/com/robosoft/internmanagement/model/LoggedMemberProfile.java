package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class LoggedMemberProfile {

    private String name;

    private long mobileNumber;

    private MultipartFile photo;

    private String designation;

}
