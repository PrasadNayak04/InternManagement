package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.model.ResponseData;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.service.MemberServices;
import com.robosoft.internmanagement.service.StorageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@CrossOrigin( methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS}, origins ="http://localhost:4200")
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
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notificationDisplay, AppConstants.RECORD_NOT_EXIST));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notificationDisplay, AppConstants.SUCCESS));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpServletRequest request){

        List<?> notifications = memberServices.getNotifications(request);
        if(notifications.size() == 0)
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notifications, AppConstants.NO_RESULT_SUCCESS));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notifications, AppConstants.SUCCESS));

    }

    @PostMapping("/event-creation")
    public ResponseEntity<?> createEvent(@RequestBody Event event, HttpServletRequest request){
        if(memberServices.createEvent(event, request)){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("EVENT CREATION SUCCESSFUL", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("EVENT CREATION FAILED", AppConstants.TASK_FAILED));
    }

    @PutMapping("/event-status-update")
    public ResponseEntity<?> reactEventInvite(@RequestParam int notificationId, @RequestParam String status, HttpServletRequest request){
        if(memberServices.reactToEventInvite(notificationId, status, request)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("INVITATION_STATUS UPDATED", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("FAILED_TO_UPDATE_EVENT_STATUS", AppConstants.TASK_FAILED));
    }

    @PutMapping("/notification-removal/{notificationId}")
    public ResponseEntity<?> removeNotification(@PathVariable int notificationId, HttpServletRequest request){
        if(memberServices.removeNotification(notificationId, request))
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("SUCCESS", AppConstants.SUCCESS));

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>("FAILED", AppConstants.TASK_FAILED));
    }

}
