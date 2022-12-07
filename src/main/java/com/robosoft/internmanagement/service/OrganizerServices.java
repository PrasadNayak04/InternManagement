package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface OrganizerServices
{
    ResponseData<?> takeInterview(AssignBoard board, HttpServletRequest request);

    int getAnyLocationVacancy(String designation);

    List<?> assignedCandidates(HttpServletRequest request);
}
