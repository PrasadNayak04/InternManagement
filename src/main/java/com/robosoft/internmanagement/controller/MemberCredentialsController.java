package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.modelAttributes.MemberCredentials;
import com.robosoft.internmanagement.modelAttributes.MemberProfile;
import com.robosoft.internmanagement.service.EmailServices;
import com.robosoft.internmanagement.service.MemberServices;
import com.robosoft.internmanagement.service.jwtSecurity.JwtUserDetailsService;
import com.robosoft.internmanagement.service.jwtSecurity.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping(value = "/intern-management/member-credentials")
public class MemberCredentialsController {

    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private MemberServices memberServices;

    @Autowired
    private EmailServices emailServices;

    @PostMapping(value = "/email-verification")
    public ResponseEntity<?> verifyMail(@RequestBody MemberCredentials memberCredentials){
        boolean mailSent = emailServices.sendRegistrationOtp(memberCredentials.getEmailId());
        if(mailSent){
            return ResponseEntity.ok(memberCredentials.getEmailId());
        }else{
            return ResponseEntity.ok("false");
        }
    }

    @PostMapping(value = "/register")
    public String registerMember(@Valid @RequestBody MemberProfile memberProfile, HttpServletRequest request){
        return memberServices.registerMember(memberProfile, request);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createToken(@RequestBody Member member, HttpServletRequest request) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(member.getEmailId(), member.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(member.getEmailId());
            final String jwtToken = tokenManager.generateJwtToken(userDetails);
            return ResponseEntity.ok(jwtToken);
        } catch (Exception e) {
            return ResponseEntity.ok("false");
        }
    }

    @PostMapping("/otp")
    public ResponseEntity<?> sendMail(@RequestBody MemberCredentials memberCredentials){
        boolean mailSent = emailServices.sendEmail(memberCredentials.getEmailId());

        if(mailSent){
            return ResponseEntity.ok("true");
        }else{
            return ResponseEntity.ok("false");
        }
    }

    @PutMapping(value = "/otp-verification", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> verify(@RequestBody MemberCredentials memberCredentials)
    {
        String response = emailServices.verification(memberCredentials.getEmailId(), memberCredentials.getOtp());
        System.out.println(response);
        if (response.equals("VERIFIED"))
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(new MemberCredentials(memberCredentials.getName(), memberCredentials.getEmailId()), AppConstants.SUCCESS));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(response, AppConstants.TASK_FAILED));
    }

    @PatchMapping("/password-update")
    public ResponseEntity<?> updatePassword(@RequestBody Member member){
        int updateStatus = memberServices.updatePassword(member);
        if(updateStatus == 1)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("TASK SUCCESSFUL", AppConstants.SUCCESS));
        else if (updateStatus == -1)
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>("CHOOSE DIFFERENT PASSWORD", AppConstants.TASK_FAILED));
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>("TASK FAILED", AppConstants.TASK_FAILED));
    }

}
