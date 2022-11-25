package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.modelAttributes.ForgotPassword;
import com.robosoft.internmanagement.modelAttributes.Member;
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

@RestController
@CrossOrigin
@RequestMapping(value = "/member-credentials")
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


    @PostMapping(value = "/register")
    public String registerMember(@RequestBody MemberProfile memberProfile, HttpServletRequest request){
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
    public ResponseEntity<?> sendMail(@RequestBody ForgotPassword password){
        boolean mailSent = emailServices.sendEmail(password);

        if(mailSent){
            return ResponseEntity.ok("true");
        }else{
            return ResponseEntity.ok("false");
        }
    }

    @PostMapping(value = "/email-verification")
    public ResponseEntity<?> verifyMail(@RequestBody ForgotPassword password){
        boolean mailSent = emailServices.sendRegistrationOtp(password);
        if(mailSent){
            return ResponseEntity.ok("true");
        }else{
            return ResponseEntity.ok("false");
        }
    }

    @PutMapping(value = "/otp-verification")
    public ResponseEntity<?> verify(@RequestBody ForgotPassword password)
    {
        return ResponseEntity.ok(emailServices.verification(password));
    }

    @PatchMapping("/password-update")
    public ResponseEntity<?> updatePassword(@RequestBody Member member){
        int updateStatus = memberServices.updatePassword(member);
        if(updateStatus == 1)
            return ResponseEntity.ok("true");
        else if (updateStatus == -1)
            return ResponseEntity.ok("same");
        return ResponseEntity.ok("false");
    }

}
