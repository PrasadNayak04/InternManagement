package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;

import javax.servlet.http.HttpServletRequest;

public interface OrganizerServices
{
    ResponseData<?> takeInterview(AssignBoard board, HttpServletRequest request);

    int getAnyLocationVacancy(String designation);

}
