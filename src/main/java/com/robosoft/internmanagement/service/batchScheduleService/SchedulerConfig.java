package com.robosoft.internmanagement.service.batchScheduleService;

import com.robosoft.internmanagement.model.EventReminder;
import com.robosoft.internmanagement.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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

        String query = "select eventId, creatorEmail, title, venue, location, date from Events where 0 <= (date-CURDATE()) <= 5";

        jdbcTemplate.query(query,
                (resultSet, no) -> {
                    EventReminder eventReminder = new EventReminder();

                    eventReminder.setEventId(resultSet.getInt(1));
                    eventReminder.setCreatorEmail(resultSet.getString(2));
                    eventReminder.setTitle(resultSet.getString(3));
                    eventReminder.setVenue(resultSet.getString(4));
                    eventReminder.setLocation(resultSet.getString(5));
                    eventReminder.setDate(resultSet.getDate(6));

                    String query0 = "select invitedEmail from EventsInvites where eventId = ? and status = 'JOIN'";
                    List<String> invitedEmails = jdbcTemplate.queryForList(query0, String.class, eventReminder.getEventId());

                    String creatorName;
                    for(String invitedEmail : invitedEmails){
                        creatorName = memberService.getMemberNameByEmail(eventReminder.getCreatorEmail());
                        String message = "You have an upcoming event '" + eventReminder.getTitle() + "' on " + eventReminder.getDate().toLocalDate().getYear() + "," + eventReminder.getDate().toLocalDate().getMonth() + " " + eventReminder.getDate().toLocalDate().getDayOfMonth() + " At " + eventReminder.getVenue() + ", " + eventReminder.getLocation() + ". Created by " + creatorName;
                        String query2 = "insert into Notifications(emailId, message, type, eventId)  values(?,?,?,?)";
                        jdbcTemplate.update(query2, invitedEmail, message, "REMINDER", eventReminder.getEventId());
                    }
                    String message = "You have an upcoming event '" + eventReminder.getTitle() + "' on " + eventReminder.getDate().toLocalDate().getYear() + "," + eventReminder.getDate().toLocalDate().getMonth() + " " + eventReminder.getDate().toLocalDate().getDayOfMonth() + " At " + eventReminder.getVenue() + ", " + eventReminder.getLocation() + ". Created by you";
                    String query2 = "insert into Notifications(emailId, message, type, eventId)  values(?,?,?,?)";
                    jdbcTemplate.update(query2, eventReminder.getCreatorEmail(), message, "REMINDER", eventReminder.getEventId());

                    return eventReminder;

                });

    }
}
