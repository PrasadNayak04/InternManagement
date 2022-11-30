package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.DatabaseException;
import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.model.Application;
import com.robosoft.internmanagement.model.MemberModel;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AuthorityService implements AuthorityServices {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CandidateService candidateService;

    @Autowired
    MemberService memberService;

    private String query;

    public ResponseData<?> addTechnology(Technology technology, HttpServletRequest request) {

        query = "select count(designation) from technologies where designation = ? and deleted = 0";
        int count = jdbcTemplate.queryForObject(query, Integer.class, technology.getDesignation());

        if (count > 0) {
            return new ResponseData<>("FAILED", AppConstants.RECORD_ALREADY_EXIST);
        }

        int totalVacancy = 0;
        String status = "Closed";
        for (int i : technology.getLocations().values()) {
            totalVacancy += i;
        }

        if (totalVacancy > 0)
            status = "Active";

        try {
            query = "insert into technologies(designation, vacancy, status) values (?,?,?)";
            jdbcTemplate.update(query, technology.getDesignation(), totalVacancy, status);

            query = "insert into locations (designation, location, vacancy) values (?,?,?)";

            for (Map.Entry<String, Integer> entry : technology.getLocations().entrySet()) {
                jdbcTemplate.update(query, technology.getDesignation(), entry.getKey(), entry.getValue());
            }

            return new ResponseData<>("SUCCESS", AppConstants.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            query = "delete from technologies where designation = ? and deleted = 0";
            jdbcTemplate.update(query, technology.getDesignation());
            return new ResponseData<>("FAILED", AppConstants.REQUIREMENTS_FAILED);
        }

    }

    @Override
    public List<MemberModel> getAllRecruiters() {
        query = "select emailId, name, photoUrl from membersprofile where position = 'RECRUITER'";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(MemberModel.class));
    }

    public List<Application> getApplicants() {
        query = "select candidateId, imageUrl, emailId, mobileNumber, designation,location,date from applications inner join candidatesprofile using(candidateId) inner join documents using(candidateId) where candidateId NOT IN (select candidateId from assignboard where assignboard.deleted = 0) and applications.deleted = 0 and documents.deleted = 0 and candidatesprofile.deleted = 0";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Application.class));
    }

    public ResponseData<String> assignRecruiter(AssignBoard assignBoard, HttpServletRequest request) {
        if(memberService.alreadyShortlisted(assignBoard.getCandidateId(), request))
            return new ResponseData<>("FAILED", AppConstants.RECORD_ALREADY_EXIST);

        try {
            query = "select position from candidatesprofile where candidateId = ? and deleted = 0";
            String designation = jdbcTemplate.queryForObject(query, String.class, assignBoard.getCandidateId());

            if (!candidateService.isVacantPosition(designation)) {
                return new ResponseData<>("FAILED", AppConstants.REQUIREMENTS_FAILED);
            }

            query = "select name from membersprofile where emailId=? and position=?";
            jdbcTemplate.queryForObject(query, String.class, assignBoard.getRecruiterEmail(), "RECRUITER");
        } catch (Exception e) {
            throw new DatabaseException(AppConstants.RECORD_NOT_EXIST);
        }

        insertIntoAssignBoard(assignBoard);
        return new ResponseData<>("RECRUITER_ASSIGNED_SUCCESSFULLY", AppConstants.SUCCESS);

    }

    public void insertIntoAssignBoard(AssignBoard assignBoard) {
        try {
            query = "insert into assignboard(candidateId,recruiterEmail, assignDate) values(?,?,?)";
            jdbcTemplate.update(query, assignBoard.getCandidateId(), assignBoard.getRecruiterEmail(), LocalDate.now());
        } catch (Exception e1) {
            throw new DatabaseException(AppConstants.RECORD_ALREADY_EXIST);
        }
    }

}
