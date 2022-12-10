package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.MemberModel;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.modelAttributes.MemberProfile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
    Boolean updateProfile(MemberProfile memberProfile, HttpServletRequest request);
}
