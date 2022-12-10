package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.modelAttributes.MemberProfile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface MemberServices
{
    Member getMemberByEmail(String memberEmail);

    ResponseData<?> registerMember(MemberProfile memberProfile, HttpServletRequest request);

    int updatePassword(Member member);

    MemberModel createLoggedInMemberModel(String emailId);

    LoggedProfile getProfile(HttpServletRequest request);

    NotificationDisplay notification(HttpServletRequest request);

    boolean createEvent(Event event, HttpServletRequest request);

    void rollbackEvent(int eventId);

    boolean deleteExistingCandidate(int candidateId);

    boolean reactToEventInvite(int notificationId, String status, HttpServletRequest request);

    List<?> getNotifications(HttpServletRequest request);

    String encodePassword(String password);

    String getMemberNameByEmail(String email);

    boolean validPageDetails(int pageNo, Integer limit);

    boolean removeNotification(int notificationId, HttpServletRequest request);

    List<?> getAllMembers();
    Boolean updateProfile(LoggedMemberProfile memberProfile, HttpServletRequest request) throws IOException;
}
