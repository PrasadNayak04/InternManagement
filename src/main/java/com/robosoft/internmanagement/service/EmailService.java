package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.modelAttributes.CandidateInvite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Random;

@Service
public class EmailService implements EmailServices
{

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    private MemberService memberService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public boolean sendEmail(String toEmail)
    {

        boolean flag = false;

        String subject = "OTP from Intern Management";

        Random random = new Random();
        int otp;

        do
        {
            otp = random.nextInt(9999);
        }
        while(String.valueOf(otp).length() < 3);

        String message = "Please use OTP " + otp + " for your account password reset request";

        try
        {

            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            javaMailSender.send(mailMessage);
            String OTP=String.valueOf(otp);
            try
            {
                jdbcTemplate.queryForObject("select emailId from member where emailId=?", String.class,toEmail);
                jdbcTemplate.update("update forgotpasswords set otp=?,time=current_timestamp where emailId=?",OTP,toEmail);
                return flag = true;
            }
            catch (Exception e)
            {
                return flag = insert(toEmail,OTP);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    public boolean insert(String emailId,String code)
    {
        try{
            jdbcTemplate.update("insert into forgotpasswords(emailId,otp) values (?,?)",emailId,code);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String verification(String emailId, String otp)
    {
        try {
            String query = "select now()-forgotpasswords.time from forgotpasswords where emailid=?";
            long expireTime = jdbcTemplate.queryForObject(query, Long.class, emailId);
            String verifyOtp = jdbcTemplate.queryForObject("select otp from forgotpasswords where emailId=?", String.class, emailId);

            if (otp.equals(verifyOtp) && expireTime < 120) {
                return "Done";
            }
            return "Invalid OTP/Time Expired";
        }catch (Exception e)
        {
            return "Time Expired";
        }
    }

    public boolean sendInviteEmail(CandidateInvite invites, HttpServletRequest request)
    {
        String subject = "Invite from Robosoft Technologies";
        String message = "Inviting to join us as a intern.";

        try
        {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(memberService.getUserNameFromRequest(request));
            mailMessage.setTo(invites.getCandidateEmail());
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            LocalDate date = LocalDate.now();
            String query = "insert into candidatesinvites(fromEmail,candidateName,designation,mobileNumber,location,jobDetails,candidateEmail,date) values(?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(query,memberService.getUserNameFromRequest(request),invites.getCandidateName(),invites.getDesignation(),invites.getMobileNumber(),invites.getLocation(),invites.getJobDetails(),invites.getCandidateEmail(),date);
            javaMailSender.send(mailMessage);
            return true;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resendInvite(int inviteId, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        String subject = "Invite from Robosoft Technologies";
        String message = "Inviting to join us as a intern.";

        String query = "select * from candidatesinvites where candidateInviteId=?";
        CandidateInvite invites = jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(CandidateInvite.class), inviteId);
        try
        {
            String check = "select designation from candidatesinvites where candidateInviteId=? and fromEmail=?";
            jdbcTemplate.queryForObject(check, String.class,inviteId, currentUser);

            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(currentUser);
            mailMessage.setTo(invites.getCandidateEmail());
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            LocalDate date = LocalDate.now();
            String inviteQuery = "insert into candidatesinvites(fromEmail,candidateName,designation,mobileNumber,location,jobDetails,candidateEmail,date) values(?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(inviteQuery, currentUser, invites.getCandidateName(), invites.getDesignation(), invites.getMobileNumber(), invites.getLocation(), invites.getJobDetails(), invites.getCandidateEmail(), date);
            String softDelete = "update candidatesinvites set deleted = 1 where candidateInviteId= ?";
            jdbcTemplate.update(softDelete, inviteId);
            javaMailSender.send(mailMessage);
            return true;

        }catch (Exception e)
        {
            return false;
        }
    }

}
