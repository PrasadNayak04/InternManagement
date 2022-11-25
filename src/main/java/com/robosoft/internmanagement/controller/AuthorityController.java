package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.model.Application;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Technology;
import com.robosoft.internmanagement.service.AuthorityServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/authority", produces = "application/json")
public class AuthorityController
{
    @Autowired
    AuthorityServices authorityServices;

    @PostMapping("/new-technology")
    public ResponseEntity<?> addNewTechnology(@RequestBody Technology technology, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body((authorityServices.addTechnology(technology, request)));
    }
    @GetMapping("/available-recruiters")
    public ResponseEntity<?> getAllRecruiters(){
        return ResponseEntity.ok(authorityServices.getAllRecruiters());
    }

    @GetMapping("/applicants")
    public List<Application> allApplicants()
    {
        return authorityServices.getApplicants();
    }

    @PostMapping("/recruiter-assignation")
    public String setRecruiter(@ModelAttribute AssignBoard assignBoard)
    {
        return authorityServices.assignRecruiter(assignBoard);
    }

}

