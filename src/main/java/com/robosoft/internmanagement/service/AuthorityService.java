package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.Application;
import com.robosoft.internmanagement.model.MemberModel;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Service
public class AuthorityService implements AuthorityServices
{

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CandidateService candidateService;

    private String query;

    public boolean addTechnology(Technology technology, HttpServletRequest request){

        query = "select count(designation) from Technologies where designation = ? and deleted = 0";
        int count = jdbcTemplate.queryForObject(query, Integer.class, technology.getDesignation());

        if(count > 0){
            return false;
        }

        int totalVacancy = 0;
        String status = "Closed";
        for(int i : technology.getLocations().values()){
            totalVacancy += i;
        }

        if(totalVacancy > 0)
            status = "Active";

        try {
            query = "insert into Technologies(designation, vacancy, status) values (?,?,?)";
            jdbcTemplate.update(query, technology.getDesignation(), totalVacancy, status);

            query = "insert into Locations (designation, location, vacancy) values (?,?,?)";

            for (Map.Entry<String, Integer> entry : technology.getLocations().entrySet()){
                System.out.println( entry.getKey() + " " + entry.getValue());
                jdbcTemplate.update(query, technology.getDesignation(), entry.getKey(), entry.getValue());
            }
            return true;
        } catch (Exception e) {
            query = "delete from Technologies where designation = ? and deleted = 0";
            jdbcTemplate.update(query, technology.getDesignation());
            return false;
        }

    }

    @Override
    public List<?> getAllRecruiters(){
        query = "select emailId, name, photoUrl from MembersProfile where position = 'RECRUITER'";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(MemberModel.class));
    }

    public List<Application> getApplicants()
    {
        query = "select candidateId, imageUrl, emailId, mobileNumber, designation,location,date from Applications inner join CandidatesProfile using(candidateId) inner join Documents using(candidateId) where candidateId NOT IN (select candidateId from Assignboard where Assignboard.deleted = 0) and Applications.deleted = 0 and Documents.deleted = 0 and CandidatesProfile.deleted = 0";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<>(Application.class));
    }

    public String assignRecruiter(AssignBoard assignBoard)
    {
        query = "select position from CandidatesProfile where candidateId = ? and deleted = 0";
        String designation = jdbcTemplate.queryForObject(query, String.class, assignBoard.getCandidateId());

        if(!candidateService.isVacantPosition(designation)){
            return "Cannot assign since position is closed";
        }
        try
        {
            query = "select name from MembersProfile where emailId=? and position=?";
            jdbcTemplate.queryForObject(query, String.class,assignBoard.getRecruiterEmail(),"RECRUITER");

            try {
                query = "insert into Assignboard(candidateId,recruiterEmail) values(?,?)";
                jdbcTemplate.update(query,assignBoard.getCandidateId(),assignBoard.getRecruiterEmail());
                return "Recruiter Assigned Successfully";
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                return "Applicant is assigned already";
            }
        }
        catch (Exception e) {
            return "Select correct Recruiter to assign";
        }
    }

}
