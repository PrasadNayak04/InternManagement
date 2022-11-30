package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class WorkHistory {

    @NotBlank
    private String company;
    @NotBlank
    private String position;
    private Date fromDate;
    private Date toDate;
    @NotBlank
    private String location;

}
