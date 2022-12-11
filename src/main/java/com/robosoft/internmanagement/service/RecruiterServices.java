package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.ResponseData;
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

    boolean rejectAssignedCandidate(int candidateId, HttpServletRequest request);

    boolean reRecruitCandidate(int candidateId, HttpServletRequest request);

    boolean deleteCandidate(int candidateId, HttpServletRequest request);

    List<?> getAllOrganizers();

    List<?> getOrganizers(Integer limit, HttpServletRequest request);

    int getInterviewsCount(String organizerEmail, String recruiterEmail);

    Summary getSummary(Date date, HttpServletRequest request);

    int cvCount(HttpServletRequest request);

    List<?> cvAnalysisPage(Date date);

    List<?> searchDesignation(String designation);

    int updateStatus(String designation, String newStatus);

    List<String> getLocationsByDesignation(String designation);

    ExtendedCV getBasicCVDetails(int candidateId, HttpServletRequest request);

    List<Education> getEducationsHistory(int candidateId);

    List<WorkHistory> getWorkHistory(int candidateId);

    List<Link> getSocialLinks(int candidateId);

    String downloadCV(int candidateId, HttpServletRequest request);

    List<TopTechnology> getTopTechnologies(String designation);

    String getLastJobPosition(int candidateId);

    List<?> getProfileBasedOnStatus(String designation, String status, HttpServletRequest request);

    List<Application> getNotAssignedApplicants(HttpServletRequest request);

    ResponseData<?> assignOrganizer(AssignBoard assignBoard, HttpServletRequest request);

    List<?> getAssignBoardPage(HttpServletRequest request);

    List<?> getRejectedCvPage(HttpServletRequest request);

    Invite getInviteInfo(HttpServletRequest request);

    List<?> getByDay(Date date, HttpServletRequest request);

    List<?> getByMonth(Date date, HttpServletRequest request);

    List<?> getByYear(Date date, HttpServletRequest request);

    List<SentInvite> searchInvites(int value, Date date, String name, HttpServletRequest request);

    List<AssignBoardPage> assignBoardSearch(String location,HttpServletRequest request);

}
