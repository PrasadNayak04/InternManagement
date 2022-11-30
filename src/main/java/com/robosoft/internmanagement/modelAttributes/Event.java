package com.robosoft.internmanagement.modelAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Date;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Event {

    @NotBlank
    private String title;
    @NotBlank
    private String venue;
    @NotBlank
    private String location;
    private Date date;
    private int time;
    private String period;
    private String description;
    private List<String> invitedEmails;

}
