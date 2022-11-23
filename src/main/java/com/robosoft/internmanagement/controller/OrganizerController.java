package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.service.OrganizerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/intern-management/organizer")
public class OrganizerController
{
    @Autowired
    OrganizerServices organizerServices;

    @PutMapping("/interview")
    public String assignStatus(@ModelAttribute AssignBoard board, HttpServletRequest request)
    {
        return organizerServices.takeInterview(board, request);
    }

}

