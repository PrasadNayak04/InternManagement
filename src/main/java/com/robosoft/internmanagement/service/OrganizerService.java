package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.AssignBoardPage;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class OrganizerService implements OrganizerServices
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    public String takeInterview(AssignBoard board, HttpServletRequest request){
        if(!(memberService.getUserNameFromRequest(request).equals(board.getOrganizerEmail()))){
            return "You can only take interviews which are assigned to you.";
        }
        try{
            String query = "select status from Assignboard where candidateId=? and organizerEmail=? and status=? and deleted = 0";
            jdbcTemplate.queryForObject(query,String.class,board.getCandidateId(),board.getOrganizerEmail(),"NEW");

            AssignBoardPage assignBoard = memberService.getAssignBoardPageDetails(board);

            if(board.getStatus().equalsIgnoreCase("SHORTLISTED")){

                query = "select vacancy from Locations where designation = ? and location = ? and deleted = 0";
                if(jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation(), assignBoard.getLocation()) == 0){

                    query = "select vacancy from Locations where designation = ? and location = 'ANY' and deleted = 0";
                    int anyVacancy = jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation());
                    if(anyVacancy == 0) {
                        board.setStatus("REJECTED");
                        rejectCandidate(board);
                        memberService.addToResults(assignBoard, "REJECTED");
                        return "Rejected No vacancy available";
                    }
                    else {
                        assignBoard.setLocation("ANY");
                        query = "update Assignboard set status = ? where candidateId = ? and deleted = 0";
                        jdbcTemplate.update(query, board.getStatus(), board.getCandidateId());

                        System.out.println(assignBoard.getCandidateId());
                        memberService.addToResults(assignBoard, "SHORTLISTED");

                        query = "update Locations set vacancy = vacancy - 1 where designation = ? and location = 'ANY' and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());

                        query = "update Technologies set vacancy = vacancy - 1 where designation = ? and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());
                        return "Specified not available. Shortlisted to other location";
                    }
                }

                query = "update Assignboard set status = ? where candidateId = ? and deleted = 0";
                jdbcTemplate.update(query, board.getStatus(), board.getCandidateId());

                memberService.addToResults(assignBoard, "SHORTLISTED");

                query = "update Locations set vacancy = vacancy - 1 where designation = ? and location = ? and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getDesignation(), assignBoard.getLocation());

                query = "update Technologies set vacancy = vacancy - 1 where designation = ? and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getDesignation());

                query = "select vacancy from Technologies where designation = ? and deleted = 0";
                if(jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation()) == 0){
                    query = "update Technologies set status = 'CLOSED' where designation = ?";
                    jdbcTemplate.update(query, assignBoard.getDesignation());
                }
            }
            else if(board.getStatus().equalsIgnoreCase("REJECTED")){
                rejectCandidate(board);
                memberService.addToResults(assignBoard, "REJECTED");
            }
        }
        catch (Exception e)
        {
            return "Invalid information";
        }
        return "Interview Completed Successfully";
    }

    public void rejectCandidate(AssignBoard board){
        String query = "update Assignboard set status='REJECTED' where candidateId=? and organizerEmail=? and status=?";
        jdbcTemplate.update(query,board.getCandidateId(),board.getOrganizerEmail(),"NEW");
    }
    
}
