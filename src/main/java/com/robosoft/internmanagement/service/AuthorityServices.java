package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.model.Application;
import com.robosoft.internmanagement.model.MemberModel;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AuthorityServices
{
    ResponseData<?> addTechnology(Technology technology, HttpServletRequest request);
    List<MemberModel> getAllRecruiters();

    List<Application> getApplicants();

    ResponseData<String> assignRecruiter(AssignBoard assignBoard, HttpServletRequest request);
}
