package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.model.LoggedMemberProfile;
import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.modelAttributes.EventReaction;
import com.robosoft.internmanagement.service.MemberServices;
import com.robosoft.internmanagement.service.StorageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin( methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS}, origins ={"http://localhost:4200", "http://localhost:3000", "https://internmanagement.netlify.app/"})
@RequestMapping(value = "/intern-management/member")
public class MemberController {

    @Autowired
    private StorageServices storageServices;

    @Autowired
    private MemberServices memberServices;

    @GetMapping("/logged-profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request)
    {
        LoggedProfile loggedProfile = memberServices.getProfile(request);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(loggedProfile, AppConstants.SUCCESS));
    }

    @GetMapping("/notification-display")
    public ResponseEntity<?> getNotificationsDisplay(HttpServletRequest request)
    {
        NotificationDisplay notificationDisplay = memberServices.notification(request);
        if(notificationDisplay == null){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("No recent notifications", AppConstants.RECORD_NOT_EXIST));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notificationDisplay, AppConstants.SUCCESS));
    }

    @GetMapping("/members")
    public ResponseEntity<?> getAllMembers()
    {
        List<?> members = memberServices.getAllMembers();

       if(members.size() == 0)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(members, AppConstants.NO_RESULT_SUCCESS));

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(members, AppConstants.SUCCESS));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request){

        List<?> notifications = memberServices.getNotifications(request);
        if(notifications.size() == 0)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("No recent notifications", AppConstants.NO_RESULT_SUCCESS));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notifications, AppConstants.SUCCESS));

    }

    @PostMapping("/event-creation")
    public ResponseEntity<?> createEvent(@RequestBody Event event, HttpServletRequest request){
        if(memberServices.createEvent(event, request)){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("EVENT CREATION SUCCESSFUL", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("EVENT CREATION FAILED", AppConstants.TASK_FAILED));
    }

    @PostMapping("/event-status-update")
    public ResponseEntity<?> reactEventInvite(@RequestBody EventReaction eventReaction, HttpServletRequest request){
        if(memberServices.reactToEventInvite(eventReaction.getNotificationId(), eventReaction.getStatus(), request)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("INVITATION_STATUS UPDATED", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("FAILED_TO_UPDATE_EVENT_STATUS", AppConstants.TASK_FAILED));
    }

    @PutMapping("/notification-removal")
    public ResponseEntity<?> removeNotification(@RequestBody int notificationId, HttpServletRequest request){
        if(memberServices.removeNotification(notificationId, request))
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("SUCCESS", AppConstants.SUCCESS));

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("FAILED", AppConstants.TASK_FAILED));
    }

    @PutMapping("/profile-update")
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute LoggedMemberProfile memberProfile, HttpServletRequest request) throws IOException {
        if(memberServices.updateProfile(memberProfile,request)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("SUCCESS", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("FAILED", AppConstants.TASK_FAILED));
    }

    @GetMapping("/notifications-search")
    public ResponseEntity<?> searchNotification(@RequestParam String key, HttpServletRequest request){
        List<?> notifications = memberServices.searchNotifications(key, request);
        if(notifications.size() > 0)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notifications, AppConstants.RECORD_NOT_EXIST));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notifications, AppConstants.SUCCESS));
    }

}
