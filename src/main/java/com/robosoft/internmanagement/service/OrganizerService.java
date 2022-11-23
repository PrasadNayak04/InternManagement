package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.AssignBoardPage;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CandidateService candidateService;

    public String takeInterview(AssignBoard board, HttpServletRequest request){
        if(!(memberService.getUserNameFromRequest(request).equals(board.getOrganizerEmail()))){
            return "You can only take interviews which are assigned to you.";
        }
        try{
            AssignBoardPage assignBoard = memberService.getAssignBoardPageDetails(board);

            if (!candidateService.isVacantPosition(assignBoard.getDesignation())){
                return "Designation status closed";
            }

            String query = "select status from assignboard where candidateId=? and organizerEmail=? and status=? and deleted = 0";
            jdbcTemplate.queryForObject(query,String.class,board.getCandidateId(),board.getOrganizerEmail(),"NEW");

            if(board.getStatus().equalsIgnoreCase("SHORTLISTED")){

                query = "select vacancy from locations where designation = ? and location = ? and deleted = 0";
                if(jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation(), assignBoard.getLocation()) == 0){

                    query = "select vacancy from locations where designation = ? and location = 'ANY' and deleted = 0";
                    int anyVacancy = jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation());
                    if(anyVacancy == 0) {
                        board.setStatus("REJECTED");
                        rejectCandidate(board);
                        memberService.addToResults(assignBoard, "REJECTED");
                        return "Rejected No vacancy available";
                    }
                    else {
                        assignBoard.setLocation("ANY");
                        query = "update assignboard set status = ? where candidateId = ? and deleted = 0";
                        jdbcTemplate.update(query, board.getStatus(), board.getCandidateId());

                        System.out.println(assignBoard.getCandidateId());
                        memberService.addToResults(assignBoard, "SHORTLISTED");

                        query = "update locations set vacancy = vacancy - 1 where designation = ? and location = 'ANY' and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());

                        query = "update technologies set vacancy = vacancy - 1 where designation = ? and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());
                        return "Specified not available. Shortlisted to other location";
                    }
                }

                query = "update assignboard set status = ? where candidateId = ? and deleted = 0";
                jdbcTemplate.update(query, board.getStatus(), board.getCandidateId());

                memberService.addToResults(assignBoard, "SHORTLISTED");

                query = "update locations set vacancy = vacancy - 1 where designation = ? and location = ? and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getDesignation(), assignBoard.getLocation());

                query = "update technologies set vacancy = vacancy - 1 where designation = ? and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getDesignation());

                query = "select vacancy from technologies where designation = ? and deleted = 0";
                if(jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation()) == 0){
                    query = "update technologies set status = 'CLOSED' where designation = ?";
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
        String query = "update assignboard set status='REJECTED' where candidateId=? and organizerEmail=? and status=?";
        jdbcTemplate.update(query,board.getCandidateId(),board.getOrganizerEmail(),"NEW");
    }
    
}
