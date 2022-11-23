package com.robosoft.internmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class EventReminder {

    private int eventId;
    private String creatorEmail;
    private String title;
    private String venue;
    private String location;
    private Date date;

}
