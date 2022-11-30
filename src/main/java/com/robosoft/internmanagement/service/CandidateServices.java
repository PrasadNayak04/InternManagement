package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.modelAttributes.CandidateProfile;

import javax.servlet.http.HttpServletRequest;

public interface CandidateServices
{
    ResponseData candidateRegister(CandidateProfile candidateProfile, HttpServletRequest request) throws Exception;

    int getCandidateId(String candidateEmail);

    void delCandidateQuery(int candidateId);

    boolean isVacantPosition(String position);
    boolean isVacantPositionLocation(String position, String location);

    boolean alreadyShortlisted(String emailId);


}
