package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.model.AssignBoardPage;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Service
@ControllerAdvice
public class OrganizerService implements OrganizerServices
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CandidateService candidateService;

    public ResponseData<?> takeInterview(AssignBoard board, HttpServletRequest request){

        if(!interviewAssigned(board, request))
            return new ResponseData<>("FAILED", AppConstants.INVALID_INFORMATION);

        if(memberService.alreadyShortlisted(board.getCandidateId(), request))
            return new ResponseData<>("FAILED", AppConstants.RECORD_ALREADY_EXIST);

        try{
            AssignBoardPage assignBoard = memberService.getAssignBoardPageDetails(board);
            if(assignBoard == null)
                throw new Exception("FAILED");

            if (!candidateService.isVacantPosition(assignBoard.getDesignation())){
                return new ResponseData<>("FAILED", AppConstants.REQUIREMENTS_FAILED);
            }

            if(board.getStatus().equalsIgnoreCase("SHORTLISTED")){

                String query = "select vacancy from locations where designation = ? and location = ? and deleted = 0";
                if(jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getDesignation(), assignBoard.getLocation()) == 0){

                    int anyVacancy = getAnyLocationVacancy(assignBoard.getDesignation());
                    if(anyVacancy == 0) {
                        board.setStatus("REJECTED");
                        rejectCandidate(board, request);
                        memberService.addToResults(assignBoard, "REJECTED");
                        return new ResponseData<>("CANDIDATE_REJECTED", AppConstants.SUCCESS);
                    }
                    else {
                        assignBoard.setLocation("ANY");
                        query = "update assignboard set status = ? where candidateId = ? and deleted = 0";
                        jdbcTemplate.update(query, board.getStatus(), board.getCandidateId());

                        memberService.addToResults(assignBoard, "SHORTLISTED");

                        query = "update locations set vacancy = vacancy - 1 where designation = ? and location = 'ANY' and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());

                        query = "update technologies set vacancy = vacancy - 1 where designation = ? and deleted = 0";
                        jdbcTemplate.update(query, assignBoard.getDesignation());
                        return new ResponseData<>("CANDIDATE_SHORTLISTED", AppConstants.SUCCESS);
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
                rejectCandidate(board, request);
                memberService.addToResults(assignBoard, "REJECTED");
            }
        }
        catch (Exception e)
        {
            return new ResponseData<>("FAILED", AppConstants.INVALID_INFORMATION);
        }
        return new ResponseData<>("SUCCESS", AppConstants.SUCCESS);
    }

    public int getAnyLocationVacancy(String designation){
        try {
            String query = "select vacancy from locations where designation = ? and location = 'ANY' and deleted = 0";
            return jdbcTemplate.queryForObject(query, Integer.class, designation);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    public boolean interviewAssigned(AssignBoard board, HttpServletRequest request){
        try {
            String query = "select status from assignboard where candidateId=? and organizerEmail=? and status=? and deleted = 0";
            jdbcTemplate.queryForObject(query, String.class, board.getCandidateId(), memberService.getUserNameFromRequest(request), "NEW");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void rejectCandidate(AssignBoard board,HttpServletRequest request){
        String query = "update assignboard set status='REJECTED' where candidateId=? and organizerEmail=? and status=?";
        jdbcTemplate.update(query,board.getCandidateId(),memberService.getUserNameFromRequest(request),"NEW");
    }
    
}
