package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.Application;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AuthorityServices
{
    boolean addTechnology(Technology technology, HttpServletRequest request);
    List<?> getAllRecruiters();

    List<Application> getApplicants();

    String assignRecruiter(AssignBoard assignBoard);
}
