package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.modelAttributes.CandidateProfile;
import com.robosoft.internmanagement.service.CandidateServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping(value = "/candidate", produces = "application/json")
public class CandidateController {

    @Autowired
    private CandidateServices candidateServices;

    @PostMapping("/register")
    public String candidateRegister(@ModelAttribute CandidateProfile candidateProfile, HttpServletRequest request) throws Exception {
        return candidateServices.candidateRegister(candidateProfile,request);
    }

}
