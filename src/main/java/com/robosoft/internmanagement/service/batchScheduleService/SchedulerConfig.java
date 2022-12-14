package com.robosoft.internmanagement.service.batchScheduleService;

import com.robosoft.internmanagement.model.EventReminder;
import com.robosoft.internmanagement.model.Notification;
import com.robosoft.internmanagement.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MemberService memberService;

    @Scheduled(cron = "0 0 05 * * ?")
    public void scheduleByFixedRate() throws Exception {

        String query = "select eventId, creatorEmail, title, venue, location, date from events where 0 <= (date-CURDATE()) <= 5";

        jdbcTemplate.query(query,
                (resultSet, no) -> {
                    EventReminder eventReminder = new EventReminder();

                    eventReminder.setEventId(resultSet.getInt(1));
                    eventReminder.setCreatorEmail(resultSet.getString(2));
                    eventReminder.setTitle(resultSet.getString(3));
                    eventReminder.setVenue(resultSet.getString(4));
                    eventReminder.setLocation(resultSet.getString(5));
                    eventReminder.setDate(resultSet.getDate(6));

                    String query0 = "select invitedEmail from eventsinvites where eventId = ? and status = 'JOIN'";
                    List<String> invitedEmails = jdbcTemplate.queryForList(query0, String.class, eventReminder.getEventId());

                    String creatorName;
                    for(String invitedEmail : invitedEmails){
                        creatorName = memberService.getMemberNameByEmail(eventReminder.getCreatorEmail());
                        String message = "You have an upcoming event '" + eventReminder.getTitle() + "' on " + eventReminder.getDate().toLocalDate().getYear() + "," + memberService.camelCaseWord(eventReminder.getDate().toLocalDate().getMonth().toString()) + " " + eventReminder.getDate().toLocalDate().getDayOfMonth() + " At " + eventReminder.getVenue() + ", " + eventReminder.getLocation() + ". Created by " + creatorName;
                        String query2 = "insert into notifications(emailId, message, type, eventId)  values(?,?,?,?)";
                        jdbcTemplate.update(query2, invitedEmail, message, "REMINDER", eventReminder.getEventId());
                    }
                    String message = "You have an upcoming event '" + eventReminder.getTitle() + "' on " + eventReminder.getDate().toLocalDate().getYear() + "," + memberService.camelCaseWord(eventReminder.getDate().toLocalDate().getMonth().toString()) + " " + eventReminder.getDate().toLocalDate().getDayOfMonth() + " At " + eventReminder.getVenue() + ", " + eventReminder.getLocation() + ". Created by you";
                    String query2 = "insert into notifications(emailId, message, type, eventId)  values(?,?,?,?)";
                    jdbcTemplate.update(query2, eventReminder.getCreatorEmail(), message, "REMINDER", eventReminder.getEventId());

                    return eventReminder;

                });

    }

    @Scheduled(cron = "*/60 * * * * *")
    public void updateEventInviteStatus(){
        String query = "select notificationId, message, date(notifications.date), type from notifications inner join events using(eventId) where events.date<curdate() and events.deleted = 0 and notifications.deleted = 0";

        List<Integer> eventIds = new ArrayList<>();
        jdbcTemplate.query(query, (resultSet, no) ->
        {
            Notification notification = new Notification();
            notification.setNotificationId(resultSet.getInt(1));
            notification.setMessage(resultSet.getString(2));
            Date date = resultSet.getDate(3);
            notification.setType(resultSet.getString(4));

            String subQuery = "select eventId, creatorEmail, title from events inner join notifications using(eventId) where notificationId = ?";
            jdbcTemplate.queryForObject(subQuery, (resultSet1, no1) ->
            {
                int eventId = resultSet1.getInt(1);
                String creatorEmail = resultSet1.getString(2);
                String title = resultSet1.getString(3);

                String message = "You had an expired event invitation '" + memberService.camelCaseWord(title) + "' from " + memberService.getMemberNameByEmail(creatorEmail) + " on " + memberService.camelCaseWord(date.toLocalDate().getMonth().toString()) + " " + date.toLocalDate().getDayOfMonth();

                String innerQuery= "update notifications inner join events using(eventId) inner join eventsinvites using(eventId) set message = ?, type = 'OTHERS' where events.date < curdate() and eventsinvites.status is null and notificationId = ? and eventId = ? and notifications.emailId != ? and events.deleted = 0 and notifications.deleted = 0";
                jdbcTemplate.update(innerQuery, message, notification.getNotificationId(), eventId, creatorEmail);

                eventIds.add(eventId);
                return null;
            }, notification.getNotificationId());

            return notification;
        });

        for(int eventId : eventIds){
            String innerSubQuery = "update eventsinvites set status = 'DECLINE'  where eventId = ? and status is null and deleted = 0";
            jdbcTemplate.update(innerSubQuery, eventId);
        }

    }

}
