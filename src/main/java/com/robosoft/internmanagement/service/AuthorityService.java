package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.DatabaseException;
import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
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
        query = "select candidateId, emailId, name, imageUrl, mobileNumber, designation,location,date from applications inner join candidatesprofile using(candidateId) inner join documents using(candidateId) where candidateId NOT IN (select candidateId from assignboard where assignboard.deleted = 0) and applications.deleted = 0 and documents.deleted = 0 and candidatesprofile.deleted = 0";
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

    public List<?> viewOpenings() {
        System.out.println("inside view openings");
        List<Openings> openingsList = new ArrayList<>();
        query = "select distinct(designation) from locations";
    try{
        openingsList =  jdbcTemplate.query(query,
                (resultSet, no) -> {
                    Openings openings =new Openings();
                    openings.setDesignation(resultSet.getString(1));
                    List<Location> locations = getOpening(openings.getDesignation());
                    openings.setLocation(locations);
                    return openings;
                });
    }catch(Exception exception){
        exception.printStackTrace();
    }
        return openingsList;
    }

    public List<Location> getOpening(String designation) {
        return jdbcTemplate.query("select location,vacancy from locations where designation = ? and deleted = 0",
                new BeanPropertyRowMapper<>(Location.class),designation);
    }


}
