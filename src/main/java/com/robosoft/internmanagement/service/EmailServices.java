package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.modelAttributes.CandidateInvite;

import javax.servlet.http.HttpServletRequest;

public interface EmailServices
{
    boolean sendEmail(String toEmail);

    boolean sendRegistrationOtp(String toEmail);

    boolean insert(String emailId,String code);

    String verification(String emailId,String otp);

    boolean sendInviteEmail(CandidateInvite invites, HttpServletRequest request);

    boolean resendInvite(int inviteId, HttpServletRequest request);
}
