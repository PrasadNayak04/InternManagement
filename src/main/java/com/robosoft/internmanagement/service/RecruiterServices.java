package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.exception.ResponseData;
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

    PageData<?> cvAnalysisPage(Date date, int pageNo, int limit);

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

    PageData<?> getProfileBasedOnStatus(String designation, String status, int pageNo, int limit, HttpServletRequest request);

    List<Application> getNotAssignedApplicants(HttpServletRequest request);

    ResponseData<?> assignOrganizer(AssignBoard assignBoard, HttpServletRequest request);

    PageData<?> getAssignBoardPage(int pageNo, int limit, HttpServletRequest request);

    PageData<?> getRejectedCvPage(int pageNo, int limit, HttpServletRequest request);

    Invite getInviteInfo(HttpServletRequest request);

    PageData<?> getByDay(Date date, int pageNo, int limit, HttpServletRequest request);

    PageData<?> getByMonth(Date date, int pageNo, int limit, HttpServletRequest request);

    PageData<?> getByYear(Date date, int pageNo, int limit, HttpServletRequest request);

    List<SentInvite> searchInvites(int value, Date date, String name, HttpServletRequest request);
}
