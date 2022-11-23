package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.modelAttributes.AssignBoard;

import javax.servlet.http.HttpServletRequest;

public interface OrganizerServices
{
    String takeInterview(AssignBoard board, HttpServletRequest request);

}
