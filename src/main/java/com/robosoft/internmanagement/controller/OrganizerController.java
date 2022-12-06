package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.service.OrganizerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin( methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS}, origins ="http://localhost:4200")
@RequestMapping(value = "/intern-management/organizer" , produces = "application/json")
public class OrganizerController
{
    @Autowired
    OrganizerServices organizerServices;

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

