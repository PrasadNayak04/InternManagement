package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.AssignBoardPage;
import com.robosoft.internmanagement.model.LoggedProfile;
import com.robosoft.internmanagement.model.NotificationDisplay;
import com.robosoft.internmanagement.model.Notification;
import com.robosoft.internmanagement.modelAttributes.AssignBoard;
import com.robosoft.internmanagement.modelAttributes.Event;
import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.modelAttributes.MemberProfile;
import com.robosoft.internmanagement.service.jwtSecurity.BeanStore;
import com.robosoft.internmanagement.service.jwtSecurity.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Service
public class MemberService implements MemberServices
{

    @Autowired
    private BeanStore beanStore;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TokenManager tokenManager;

    String query;


    public Member getMemberByEmail(String memberEmail){
        try {
            System.out.println("inside member email" + memberEmail);
            query = "select password, role from members where emailId = ?";
            Member member = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(Member.class), memberEmail);
            System.out.println(member.getRole());
            return new Member(memberEmail, member.getPassword(), member.getRole());

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String registerMember(MemberProfile memberProfile, HttpServletRequest request){

        query = "select count(emailId) from membersprofile where emailId = ? and deleted = 0";
        int count = jdbcTemplate.queryForObject(query, Integer.class, memberProfile.getEmailId());

        if(count > 0){
            return "Already registered";
        }

        memberProfile.setPassword(encodePassword(memberProfile.getPassword()));
        try{

            query = "insert into members(emailId, password, role) values(?,?,?)";
            jdbcTemplate.update(query, memberProfile.getEmailId(), memberProfile.getPassword(), "ROLE_" + memberProfile.getPosition().toUpperCase());

            String photoDownloadUrl = storageService.singleFileUpload(memberProfile.getPhoto(), memberProfile.getEmailId(), request, "MEMBER");

            query = "insert into membersprofile(name, emailId, photoUrl, mobileNumber, designation, position) values (?,?,?,?,?,?)";
            jdbcTemplate.update(query, memberProfile.getName(), memberProfile.getEmailId(), photoDownloadUrl, memberProfile.getMobileNumber(), memberProfile.getDesignation(), memberProfile.getPosition());

            return "User credentials saved";

        } catch(Exception e){
            query = "delete from members where emailId = ? and deleted = 0";
            jdbcTemplate.update(query, memberProfile.getEmailId());
            return "Unable to save user credentials";
        }

    }

    public int updatePassword(Member member){
        try{
            if(member.getPassword().equals("") || member.getPassword().contains(" ")){
                throw new Exception("Empty password field.");
            }
            query = "select password from members where emailId = ?";
            String password = jdbcTemplate.queryForObject(query, String.class, member.getEmailId());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if(encoder.matches(member.getPassword(), password)){
                return -1;
            }

            query = "update members set password = ? where emailId = ?";
            jdbcTemplate.update(query, encodePassword(member.getPassword()), member.getEmailId());
            String message = "You have changed your password successfully";
            try{
                query = "insert into notifications(emailId, message, type) values (?,?,?)";
                jdbcTemplate.update(query, member.getEmailId(), message, "OTHERS");
            }catch(Exception exception){
                query = "update members set password = ? where emailId = ?";
                jdbcTemplate.update(query, password, member.getEmailId());
                exception.printStackTrace();
                return 0;
            }
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public LoggedProfile getProfile(HttpServletRequest request)
    {

        query = "select name,designation,photoUrl as profileImage from membersprofile where emailId=?";
        return jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(LoggedProfile.class), getUserNameFromRequest(request));
    }

    public NotificationDisplay notification(HttpServletRequest request) {

        String notification = "select notificationId, message, date from notifications where notificationId = ? and deleted = 0";

        Notification notifications = jdbcTemplate.queryForObject("select notificationId, type from notifications where emailId=? and deleted = 0 order by notificationId desc limit 1", new BeanPropertyRowMapper<>(Notification.class), getUserNameFromRequest(request));
        int eventId = jdbcTemplate.queryForObject("select eventId from notifications where notificationId = ? and deleted = 0", Integer.class, notifications.getNotificationId());
        if (notifications.getType().equalsIgnoreCase("INVITE")) {
            String profileImage = "select photoUrl from notifications inner join eventsinvites using(eventId) inner join membersprofile on eventsinvites.invitedEmail = membersprofile.emailId where notifications.eventId=? and type = 'INVITE' and membersprofile.deleted = 0 and notifications.deleted = 0 and eventsinvites.deleted = 0 group by invitedEmail";
            List<String> images = jdbcTemplate.queryForList(profileImage, String.class, eventId);
            NotificationDisplay display = jdbcTemplate.queryForObject(notification, new BeanPropertyRowMapper<>(NotificationDisplay.class), notifications.getNotificationId());
            for (String image : images){
                System.out.println(image);
            }
            display.setImages(images);
            return display;
        } else {
            NotificationDisplay display = jdbcTemplate.queryForObject(notification, new BeanPropertyRowMapper<>(NotificationDisplay.class), notifications.getNotificationId());
            return display;
        }
    }

    public boolean createEvent(Event event, HttpServletRequest request){

        String currentUser = getUserNameFromRequest(request);

        query = "insert into events (creatorEmail, title, venue, location, date, time, period, description) values(?,?,?,?,?,?,?,?)";
        int eventId = 0;
        try{
            jdbcTemplate.update(query, currentUser, event.getTitle(), event.getVenue(), event.getLocation(), event.getDate(), event.getTime(), event.getPeriod(), event.getDescription());

            query = "select max(eventId) from events where creatorEmail = ? and deleted = 0";
            eventId = jdbcTemplate.queryForObject(query, Integer.class, currentUser);

            query = "insert into eventsinvites (eventId, invitedEmail) values (?,?)";
            for(String invitedEmail : event.getInvitedEmails()){
                jdbcTemplate.update(query, eventId, invitedEmail);
            }

            query = "insert into notifications(emailId, message, type, eventId) values (?,?,?,?)";
            String message = "Event " + event.getTitle() + " created successfully on " + event.getDate().toLocalDate().getYear() + ", " + event.getDate().toLocalDate().getMonth() + " " + event.getDate().toLocalDate().getDayOfMonth();
            jdbcTemplate.update(query, currentUser, message, "OTHERS", eventId);

            query = "insert into notifications(emailId, message, type, eventId) values (?,?,?,?)";
            message = getMemberNameByEmail(currentUser) + " invited you to Join a Event " + event.getTitle() + " in " + event.getVenue() + " on " + event.getDate().toLocalDate().getYear() + ", " + event.getDate().toLocalDate().getMonth() + " " + event.getDate().toLocalDate().getDayOfMonth() + ". Would you like to join this event?";
            for(String invitedEmail : event.getInvitedEmails()){
                jdbcTemplate.update(query, invitedEmail, message, "INVITE", eventId);
            }

            return true;

        } catch (Exception e) {
            rollbackEvent(eventId);
            return false;
        }
    }

    public void rollbackEvent(int eventId){
        query = "delete from events where eventId = ?";
        jdbcTemplate.update(query, eventId);
        query = "delete from notifications where eventId = ?";
        jdbcTemplate.update(query, eventId);
    }

    public boolean reactToEventInvite(int notificationId, String status, HttpServletRequest request){

        query = "select eventId from notifications where notificationId = ?";
        try{
            //Event invite table status update
            int eventId = jdbcTemplate.queryForObject(query, Integer.class, notificationId);
            query = "update eventsinvites set status = ? where eventId = ? and invitedEmail = ?";
            int inviteExist = jdbcTemplate.update(query, status, eventId, getUserNameFromRequest(request));
            if(inviteExist == 0){
                throw new Exception("Event invite not exists");
            }
            //Update event creators notification
            query = "select creatorEmail from events where eventId = ?";
            String eventCreator = jdbcTemplate.queryForObject(query, String.class, eventId);

            query = "select title from events where eventId = ?";
            String eventTitle = jdbcTemplate.queryForObject(query, String.class, eventId);
            String message =  getMemberNameByEmail(getUserNameFromRequest(request)) + " accepted your event " + eventTitle;
            query = "insert into notifications(emailId, message, type) values (?,?,?)";
            jdbcTemplate.update(query, eventCreator, message, "OTHERS");

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public List<?> getNotifications(int pageNo, int limit, HttpServletRequest request){
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from notifications where emailId = ? and deleted =0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, getUserNameFromRequest(request));
        }
        query = "select notificationId, message, date, type from notifications where emailId = ? and deleted =0 order by notificationId desc limit ?, ?";
        List<Notification> notifications = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Notification.class), getUserNameFromRequest(request), offset, limit);
        if(pageNo ==1) {
            return Arrays.asList(totalCount, notifications.size(), notifications);
        }
        return Arrays.asList(notifications.size(), notifications);
    }

    public String encodePassword(String password){
        return beanStore.passwordEncoder().encode(password);
    }

    public String getMemberNameByEmail(String email){
        String query = "select name from membersprofile where emailId = ?";
        try{
            return jdbcTemplate.queryForObject(query, String.class, email);
        }catch (Exception e){
            System.out.println("catch");
            return null;
        }
    }

    public boolean validPageDetails(int pageNo, Integer limit){
        if(limit == null){
            return true;
        }
        if(pageNo < 1 || limit < 1)
            return false;
        return true;
    }

    public String getUserNameFromRequest(HttpServletRequest request){
        String token = request.getHeader("Authorization").substring(7);
        System.out.println(tokenManager);
        return tokenManager.getUsernameFromToken(token);
    }

    public void addToResults(AssignBoardPage assignBoard, String result){
        try {
            String query = "insert into results(candidateId, designation, location, result) values(?,?,?,?)";
            jdbcTemplate.update(query, assignBoard.getCandidateId(), assignBoard.getDesignation(), assignBoard.getLocation(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssignBoardPage getAssignBoardPageDetails(AssignBoard board){
        try {
            String query = "select candidateId, designation, location from applications  where candidateId = ? and deleted = 0";
            return jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(AssignBoardPage.class), board.getCandidateId());
        } catch (Exception e) {
            return null;
        }
    }

}
