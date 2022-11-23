package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.modelAttributes.MemberProfile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface MemberServices
{
    Member getMemberByEmail(String memberEmail);

    String registerMember(MemberProfile memberProfile, HttpServletRequest request);

    int updatePassword(Member member);

    LoggedProfile getProfile(HttpServletRequest request);

    NotificationDisplay notification(HttpServletRequest request);

    boolean createEvent(Event event, HttpServletRequest request);

    void rollbackEvent(int eventId);

    boolean reactToEventInvite(int notificationId, String status, HttpServletRequest request);

    List<?> getNotifications(int pageNo, int limit, HttpServletRequest request);

    String encodePassword(String password);

    String getMemberNameByEmail(String email);

    boolean validPageDetails(int pageNo, Integer limit);
}
