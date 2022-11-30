package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.ResponseData;
import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.model.PageData;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.service.MemberServices;
import com.robosoft.internmanagement.service.StorageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin
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
    public ResponseEntity<?> getNotifications(HttpServletRequest request)
    {
        NotificationDisplay notificationDisplay = memberServices.notification(request);
        if(notificationDisplay == null){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>(notificationDisplay, AppConstants.RECORD_NOT_EXIST));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(notificationDisplay, AppConstants.SUCCESS));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam int pageNo, @RequestParam int limit, HttpServletRequest request){
        if(!memberServices.validPageDetails(pageNo, limit)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>("INVALID PAGE DETAILS", AppConstants.INVALID_INFORMATION));
        }
        PageData<?> pageData = memberServices.getNotifications(pageNo, limit, request);
        if(pageData == null)
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>(pageData, AppConstants.RECORD_NOT_EXIST));
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>(pageData, AppConstants.SUCCESS));

    }

    @PostMapping("/event-creation")
    public ResponseEntity<?> createEvent(@RequestBody Event event, HttpServletRequest request){
        if(memberServices.createEvent(event, request)){
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseData<>("EVENT CREATION SUCCESSFUL", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseData<>("EVENT CREATION FAILED", AppConstants.TASK_FAILED));
    }

    @PatchMapping("/event-status-update")
    public ResponseEntity<?> reactEventInvite(@RequestParam int notificationId, @RequestParam String status, HttpServletRequest request){
        if(memberServices.reactToEventInvite(notificationId, status, request)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseData<>("INVITATION_STATUS UPDATED", AppConstants.SUCCESS));
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ResponseData<>("FAILED_TO_UPDATE_EVENT_STATUS", AppConstants.TASK_FAILED));
    }

    @GetMapping("/fetch/{folderName}/{fileName}")
    public ResponseEntity<?> getFile(@PathVariable String folderName, @PathVariable String fileName, HttpServletRequest request) throws IOException {
        final String filePath = "src\\main\\resources\\static\\documents\\" + folderName + "\\" + fileName;
        Path path = Paths.get(filePath);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        String contentType = storageServices.getContentType(request, resource, fileName);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }




}
