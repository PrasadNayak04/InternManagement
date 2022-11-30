package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Education {

    @NotBlank
    private String institution;
    @NotBlank
    private String grade;
    private Date fromDate;
    private Date toDate;
    @NotBlank
    private String location;

}
