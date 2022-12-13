package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.DatabaseException;
import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
            return new ResponseData<>("Already exists", AppConstants.RECORD_ALREADY_EXIST);
        }

        query = "select count(designation) from technologies where designation = ? and deleted = 1";
        int deletedCount = jdbcTemplate.queryForObject(query, Integer.class, technology.getDesignation());

        if (deletedCount > 0) {
            updateLocation(technology);
            return new ResponseData<>("New technology added", AppConstants.SUCCESS);
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
        query = "select emailId, name, photoUrl, count(assignboard.recruiterEmail) as applicantsCount from membersprofile left join assignboard on membersprofile.emailId = assignboard.recruiterEmail where position = 'RECRUITER' group by membersprofile.emailId";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(MemberModel.class));
    }

    public List<Application> getApplicants() {
        query = "select candidateId, emailId, name, imageUrl, mobileNumber, applications.designation, technologies.status, location,date from applications inner join candidatesprofile using(candidateId) inner join documents using(candidateId) inner join technologies on technologies.designation = candidatesprofile.position  where candidateId NOT IN (select candidateId from assignboard where assignboard.deleted = 0) and applications.deleted = 0 and documents.deleted = 0 and candidatesprofile.deleted = 0 and technologies.deleted = 0";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Application.class));
    }

    public ResponseData<String> assignRecruiter(AssignBoard assignBoard, HttpServletRequest request) {
        if(memberService.alreadyShortlisted(assignBoard.getCandidateId(), request))
            return new ResponseData<>("Already shortlisted", AppConstants.RECORD_ALREADY_EXIST);

        try {
            query = "select position from candidatesprofile where candidateId = ? and deleted = 0";
            String designation = jdbcTemplate.queryForObject(query, String.class, assignBoard.getCandidateId());

            if (!candidateService.isVacantPosition(designation)) {
                return new ResponseData<>("This position is currently closed/No vacancy available", AppConstants.REQUIREMENTS_FAILED);
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
        List<Openings> openingsList = new ArrayList<>();
        query = "select technologyId, designation, status from technologies where deleted = 0";
        try{
            openingsList =  jdbcTemplate.query(query,
                    (resultSet, no) -> {
                        Openings openings =new Openings();
                        openings.setTechnologyId(resultSet.getInt(1));
                        openings.setDesignation(resultSet.getString(2));
                        openings.setStatus(resultSet.getString(3));
                        List<Location> locations = getOpening(openings.getDesignation());
                        openings.setLocation(locations);
                        return openings;
                    });
        }catch(Exception exception){
            exception.printStackTrace();
            return Arrays.asList();
        }
        return openingsList;
    }

    public List<Location> getOpening(String designation) {
        return jdbcTemplate.query("select locationId, location,vacancy from locations where designation = ? and deleted = 0",
                new BeanPropertyRowMapper<>(Location.class),designation);
    }

    public List<?> getAllLocations(int technologyId){
        query = "select count(technologyId) from technologies where technologyId = ? and deleted = 0";
        if(jdbcTemplate.queryForObject(query, Integer.class, technologyId) == 0)
            return null;
        query = "select locationId, location, locations.vacancy from locations inner join technologies using(designation) where technologyId = ? and technologies.deleted = 0 and locations.deleted = 0";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Location.class), technologyId);
    }

    public boolean updateLocation(Technology technology){
        int result = 1;
        int totalVacancy = 0;
        for(Map.Entry<String, Integer> entry : technology.getLocations().entrySet()) {
            query = "update locations set vacancy = ? where location = ? and designation = ? and deleted = 0";
            result *= jdbcTemplate.update(query, entry.getValue(), entry.getKey(), technology.getDesignation());
            totalVacancy += entry.getValue();
        }
        query = "update technologies set vacancy = ? where designation = ? and deleted = 0";
        jdbcTemplate.update(query, totalVacancy, technology.getDesignation());

        return result > 0;
    }

    public boolean updateDesignationStatus(String designation, String newStatus){
        try {
            query = "update technologies set status = ? where designation = ? and deleted = 0";
            return jdbcTemplate.update(query, newStatus, designation) > 0;
        } catch (Exception e) {
            throw new DatabaseException(AppConstants.INVALID_INFORMATION);
        }

    }

}
