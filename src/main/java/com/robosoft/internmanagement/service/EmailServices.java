package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.modelAttributes.CandidateInvite;
import com.robosoft.internmanagement.modelAttributes.ForgotPassword;

import javax.servlet.http.HttpServletRequest;

public interface EmailServices
{
    boolean sendRegistrationOtp(ForgotPassword password);
    boolean sendEmail(ForgotPassword password);

    boolean insert(String emailId,String code);

    String verification(ForgotPassword password);

    boolean sendInviteEmail(CandidateInvite invites, HttpServletRequest request);

    boolean resendInvite(int inviteId, HttpServletRequest request);
}
