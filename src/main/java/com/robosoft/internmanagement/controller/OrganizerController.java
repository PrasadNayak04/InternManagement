package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.service.OrganizerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin( methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS}, origins ={"http://localhost:4200", "http://localhost:3000"})
@RequestMapping(value = "/intern-management/organizer" , produces = "application/json")
public class OrganizerController
{
    @Autowired
    OrganizerServices organizerServices;

    @GetMapping("/candidates-assigned")
    public ResponseEntity<?> getAllCandidates(HttpServletRequest request){
        List<?> candidates = organizerServices.assignedCandidates(request);
        if(candidates.size() == 0)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("Failed", AppConstants.INVALID_INFORMATION));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(candidates, AppConstants.SUCCESS));
    }

    @PutMapping("/interview")
    public ResponseEntity<?> assignStatus(@RequestBody AssignBoard board, HttpServletRequest request)
    {
        ResponseData<?> result = organizerServices.takeInterview(board, request);
        if(result.getResult().getOpinion().equals("F"))
            return ResponseEntity.status(HttpStatus.OK).body(result);
        else
            return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}

