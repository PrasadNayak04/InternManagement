package com.robosoft.internmanagement.controller;

import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
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
@RequestMapping("/intern-management/member")
public class MemberController {

    @Autowired
    private StorageServices storageServices;

    @Autowired
    private MemberServices memberServices;

    @GetMapping("/logged-profile")
    public LoggedProfile getProfile(HttpServletRequest request)
    {
        return memberServices.getProfile(request);
    }

    @GetMapping("/notification-display")
    public NotificationDisplay getNotifications(HttpServletRequest request)
    {
        return memberServices.notification(request);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam int pageNo, @RequestParam int limit, HttpServletRequest request){
        if(!memberServices.validPageDetails(pageNo, limit)){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Invalid page details");
        }
        return ResponseEntity.ok(memberServices.getNotifications(pageNo, limit, request));
    }

    @PostMapping("/event-creation")
    public ResponseEntity<?> createEvent(@ModelAttribute Event event, HttpServletRequest request){
        if(memberServices.createEvent(event, request)){
            return ResponseEntity.status(HttpStatus.CREATED).body("Event created successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Event creation failed!");
    }

    @PatchMapping("/event-status-update")
    public ResponseEntity<?> reactEventInvite(@RequestParam int notificationId, @RequestParam String status, HttpServletRequest request){
        if(memberServices.reactToEventInvite(notificationId, status, request)) {
            return ResponseEntity.ok("Invitation status updated");
        }
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Failed to update invitation status");
    }

    @GetMapping("/fetch/{folderName}/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String folderName, @PathVariable String fileName, HttpServletRequest request) throws IOException {
        final String filePath = "src\\main\\resources\\static\\documents\\" + folderName + "\\" + fileName;
        Path path = Paths.get(filePath);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        String contentType = storageServices.getContentType(request, resource);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
