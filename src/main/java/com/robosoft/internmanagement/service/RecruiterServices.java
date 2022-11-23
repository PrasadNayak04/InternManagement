package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Education;
import com.robosoft.internmanagement.modelAttributes.Link;
import com.robosoft.internmanagement.modelAttributes.WorkHistory;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.List;

public interface RecruiterServices
{
    List<?> getAllOrganizers();

    List<?> getOrganizer(Integer limit, HttpServletRequest request);

    int getInterviewsCount(String organizerEmail, String recruiterEmail);

    Summary getSummary(Date date, HttpServletRequest request);

    int cvCount(HttpServletRequest request);

    List<?> cvAnalysisPage(Date date, int pageNo, int limit);

    CvAnalysis searchDesignation(String designation);

    int updateStatus(String designation, String newStatus);

    List<String> getLocationsByDesignation(String designation);

    ExtendedCV getBasicCVDetails(int candidateId, HttpServletRequest request);

    List<Education> getEducationsHistory(int candidateId);

    List<WorkHistory> getWorkHistory(int candidateId);

    List<Link> getSocialLinks(int candidateId);

    String downloadCV(int candidateId, HttpServletRequest request);

    List<TopTechnology> getTopTechnologies(String designation);

    String getLastJobPosition(int candidateId);

    List<?> getProfileBasedOnStatus(String designation, String status, int pageNo, int limit, HttpServletRequest request);

    List<Application> getNotAssignedApplicants(HttpServletRequest request);

    String assignOrganizer(AssignBoard assignBoard, HttpServletRequest request);

    List<?> getAssignBoardPage(int pageNo, int limit, HttpServletRequest request);

    List<?> getRejectedCvPage(int pageNo, int limit, HttpServletRequest request);

    Invite getInviteInfo(HttpServletRequest request);

    List<?> getByDay(Date date, int pageNo, int limit, HttpServletRequest request);

    List<?> getByMonth(Date date, int pageNo, int limit, HttpServletRequest request);

    List<?> getByYear(Date date, int pageNo, int limit, HttpServletRequest request);

}
