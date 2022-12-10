package com.robosoft.internmanagement.controller;

import com.cloudinary.utils.ObjectUtils;
import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.modelAttributes.CandidateProfile;
import com.robosoft.internmanagement.modelAttributes.TestD;
import com.robosoft.internmanagement.service.CandidateServices;
import com.robosoft.internmanagement.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@CrossOrigin( methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS}, origins ={"http://localhost:4200", "http://localhost:3000"})
@RestController
@RequestMapping(value = "/intern-management/candidate")
public class CandidateController {

    @Autowired
    private CandidateServices candidateServices;
    @Autowired
    StorageService storageService;

    @PostMapping(value = "/register")
    public ResponseEntity<?> candidateRegister(@Valid @ModelAttribute CandidateProfile candidateProfile, HttpServletRequest request) throws Exception {

        ResponseData<?> responseData = candidateServices.candidateRegister(candidateProfile,request);

        if(responseData.getResult().getOpinion().equals("T"))
            return ResponseEntity.status(HttpStatus.OK).body(responseData);

        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }

    @PostMapping("/register1")
    public ResponseEntity<?> upload(@Valid @ModelAttribute TestD test) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(test.getList(), AppConstants.SUCCESS));
    }

    @GetMapping("/openings")
    public ResponseEntity<?> availableDesignation() {
        List<String> designations = candidateServices.availableDesignations();
        if(designations.size()>0) {
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(designations, AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(designations, AppConstants.NO_RESULT_SUCCESS));

    }

}
