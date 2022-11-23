package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.*;
import com.robosoft.internmanagement.service.jwtSecurity.JwtFilter;
import com.robosoft.internmanagement.service.jwtSecurity.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RecruiterService implements RecruiterServices
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    String query;


    public List<?> getAllOrganizers(){
        query = "select emailId, name, photoUrl from MembersProfile where position = 'ORGANIZER'";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(MemberModel.class));
    }

    public List<?> getOrganizer(Integer limit, HttpServletRequest request)
    {
        String status = "NEW";
        List<Organizer> organizerList = new ArrayList<>();
        query = "select name, emailId, photoUrl from MembersProfile inner join Assignboard on emailId = organizerEmail where recruiterEmail = ? and status=? group by organizerEmail";

        jdbcTemplate.query(query,
                (resultSet, no) -> {
                    Organizer organizer = new Organizer();
                    organizer.setName(resultSet.getString(1));
                    organizer.setPhotoUrl(resultSet.getString(3));
                    organizer.setInterviews(getInterviewsCount(resultSet.getString(2), memberService.getUserNameFromRequest(request)));
                    organizerList.add(organizer);
                    return organizer;
                }, memberService.getUserNameFromRequest(request), status);

        Collections.sort(organizerList);

        if(limit == null){
            limit = organizerList.size();
        }
        return organizerList.subList(0, limit);
    }

    public int getInterviewsCount(String organizerEmail, String recruiterEmail){
        query = "select count(*) from Assignboard where organizerEmail = ? and recruiterEmail = ?";
        return jdbcTemplate.queryForObject(query, Integer.class, organizerEmail, recruiterEmail);
    }

    public Summary getSummary(Date date, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);

        Summary summary = new Summary();
        query = "select count(*) from Assignboard where month(assignDate)=? and year(assignDate)= ? and status = ? and recruiterEmail=?";
        int shortlisted = jdbcTemplate.queryForObject(query, Integer.class,date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(),"SHORTLISTED", currentUser);
        summary.setShortlisted(shortlisted);
        query = "select count(*) from Assignboard where month(assignDate)=? and year(assignDate)=? and status=? and recruiterEmail=?";
        int onHold = jdbcTemplate.queryForObject(query, Integer.class,date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(),"NEW", currentUser);
        summary.setOnHold(onHold);
        query = "select count(*) from Assignboard where month(assignDate)=? and year(assignDate)=? and status=? and recruiterEmail=?";
        int rejected = jdbcTemplate.queryForObject(query, Integer.class,date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(),"REJECTED", currentUser);
        summary.setRejected(rejected);
        int assigned = jdbcTemplate.queryForObject(query, Integer.class,date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(), "ASSIGNED", currentUser);
        int applications=shortlisted + onHold + rejected + assigned;
        summary.setApplications(applications);
        return summary;
    }

    public int cvCount(HttpServletRequest request)
    {
        query = "select count(candidateId) from Assignboard where recruiterEmail=? and organizerEmail is null and deleted = 0";
        return jdbcTemplate.queryForObject(query, Integer.class, memberService.getUserNameFromRequest(request));
    }

    public List<?> cvAnalysisPage(Date date, int pageNo, int limit)
    {
        List<CvAnalysis> cvAnalysisList = new ArrayList<>();
        if(date == null){
            date = Date.valueOf(LocalDate.now());
        }

        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct Applications.designation) from Applications,Technologies where Applications.designation = Technologies.designation and date=? and Applications.deleted = 0 and Technologies.deleted = 0 group by Applications.designation";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date);
        }

        query = "select Applications.designation,count(Applications.designation),date,status from Applications,Technologies where Applications.designation = Technologies.designation and date=? and Applications.deleted = 0 and Technologies.deleted = 0 group by Applications.designation limit ?, ?";
        List<CvAnalysis> cvAnalyses = jdbcTemplate.query(query,
                (resultSet, no) -> {
                    CvAnalysis cvAnalysis = new CvAnalysis();

                    cvAnalysis.setDesignation(resultSet.getString(1));
                    cvAnalysis.setApplicants(resultSet.getInt(2));
                    cvAnalysis.setReceivedDate(resultSet.getDate(3));
                    cvAnalysis.setStatus(resultSet.getString(4));
                    cvAnalysis.setLocations(getLocationsByDesignation(resultSet.getString(1)));

                    cvAnalysisList.add(cvAnalysis);
                    return cvAnalysis;
                }, date, offset, limit);

        if(pageNo ==1) {
            return List.of(totalCount, cvAnalyses.size(), cvAnalyses);
        }
        return List.of(cvAnalyses.size(), cvAnalyses);

    }

    public CvAnalysis searchDesignation(String designation)
    {
        query  = "select Applications.designation,count(Applications.designation),date,status from Applications inner join Technologies using(designation) where Applications.designation=? and Applications.deleted = 0 and Technologies.deleted = 0 group by Technologies.designation";
        try {
            return jdbcTemplate.queryForObject(query,
                    (resultSet, no) -> {
                        CvAnalysis cvAnalysis = new CvAnalysis();
                        cvAnalysis.setDesignation(resultSet.getString(1));
                        cvAnalysis.setApplicants(resultSet.getInt(2));
                        cvAnalysis.setReceivedDate(resultSet.getDate(3));
                        cvAnalysis.setStatus(resultSet.getString(4));
                        cvAnalysis.setLocations(getLocationsByDesignation(designation));
                        return cvAnalysis;
                    }, designation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int updateStatus(String designation, String newStatus){
        query = "update Technologies set status = ? where designation = ? and deleted = 0";
        return jdbcTemplate.update(query, newStatus, designation);
    }

    public List<String> getLocationsByDesignation(String designation){
        query = "select location from Locations where designation = ? and deleted = 0";
        return jdbcTemplate.queryForList(query, String.class, designation);
    }

    public ExtendedCV getBasicCVDetails(int candidateId, HttpServletRequest request){
        try{
            query = "select " + candidateId + " as candidateId, name, dob, mobileNumber,emailId, jobLocation, position, expYear, expMonth, candidateType, contactPerson, languagesKnown, softwaresWorked, skills, about, expectedCTC, attachmentUrl, imageUrl from CandidatesProfile inner join Documents using(candidateId) inner join Assignboard using(candidateId)  where recruiterEmail= ? and CandidatesProfile.candidateId = ? and CandidatesProfile.deleted = 0 and Documents.deleted = 0 and Assignboard.deleted = 0";
            return jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(ExtendedCV.class), memberService.getUserNameFromRequest(request), candidateId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Education> getEducationsHistory(int candidateId){
        query = "select * from Educations where candidateId = ? and deleted = 0";
        try {
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Education.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<WorkHistory> getWorkHistory(int candidateId){
        query = "select * from WorkHistories where candidateId = ? and deleted = 0";
        try{
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(WorkHistory.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Link> getSocialLinks(int candidateId){
        query = "select * from Links  where candidateId = ? and deleted = 0";
        try{
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Link.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public String downloadCV(int candidateId, HttpServletRequest request){
        query = "select attachmentUrl from Documents inner join Assignboard using(candidateId) where candidateId = ? and recruiterEmail = ? and Documents.deleted = 0 and Assignboard.deleted = 0";
        try{
            return jdbcTemplate.queryForObject(query, String.class, candidateId, memberService.getUserNameFromRequest(request));
        } catch (Exception e) {
            return "";
        }
    }

    public List<TopTechnology> getTopTechnologies(String designation) {
        query = "select Technologies.designation,Locations.location from Technologies left join Locations using(designation) left join Applications using(designation) where Applications.designation != ? and Technologies.deleted = 0 and Locations.deleted = 0 and Applications.deleted = 0 group by Technologies.designation order by count(Applications.designation) desc limit 5";
        List<TopTechnology> topTechnologies = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(TopTechnology.class),designation);
        List<String> locations = getLocationsByDesignation(designation);
        TopTechnology technologies = new TopTechnology(designation,locations);
        topTechnologies.add(0,technologies);
        return topTechnologies;
    }

    public String getLastJobPosition(int candidateId) {
        query = "select position from WorkHistories where candidateId = ? order by fromDate desc limit 1";
        try {
            return jdbcTemplate.queryForObject(query, String.class, candidateId);
        } catch (Exception e) {
            return null;
        }

    }

    //count mistake
    public List<?> getProfileBasedOnStatus(String designation, String status, int pageNo, int limit, HttpServletRequest request) {

        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct Applications.candidateId) from CandidatesProfile inner join Documents using(candidateId) inner join Applications using(candidateId) inner join Assignboard using(candidateId) where recruiterEmail = ? and Assignboard.status = ? and Applications.designation = ? and CandidatesProfile.deleted = 0 and Documents.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, memberService.getUserNameFromRequest(request), status, designation);
        }

        query = "select CandidatesProfile.candidateId, name, imageUrl, skills, position from CandidatesProfile inner join Documents using(candidateId) inner join Applications using(candidateId) inner join Assignboard using(candidateId) where recruiterEmail = ? and Assignboard.status = ? and Applications.designation = ? and CandidatesProfile.deleted = 0 and Documents.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0 group by Applications.applicationId limit ?, ?";
        List<ProfileAnalysis> profileAnalyses = new ArrayList<>();
        try {
            jdbcTemplate.query(query,
                    (resultSet, no) -> {
                        ProfileAnalysis profileAnalysis = new ProfileAnalysis();
                        profileAnalysis.setCandidateId(resultSet.getInt(1));
                        profileAnalysis.setName(resultSet.getString(2));
                        profileAnalysis.setImageUrl(resultSet.getString(3));
                        profileAnalysis.setPosition(getLastJobPosition(profileAnalysis.getCandidateId()));
                        profileAnalysis.setSkills(resultSet.getString(5));
                        profileAnalyses.add(profileAnalysis);
                        return profileAnalysis;
                    }, memberService.getUserNameFromRequest(request), status, designation, offset, limit);

            if(pageNo == 1) {
                return List.of(totalCount, profileAnalyses.size(), profileAnalyses);
            }
            return List.of(profileAnalyses.size(), profileAnalyses);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<Application> getNotAssignedApplicants(HttpServletRequest request)
    {
        query = "select Assignboard.candidateId, imageUrl,emailId,mobileNumber,designation,location,date from Assignboard inner join Applications using(candidateId) inner join CandidatesProfile using(candidateId) inner join Documents using(candidateId) where organizerEmail is null and recruiterEmail = ? and Applications.deleted = 0 and Assignboard.deleted = 0 and CandidatesProfile.deleted = 0 and Documents.deleted = 0";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<>(Application.class), memberService.getUserNameFromRequest(request));
    }

    public String assignOrganizer(AssignBoard assignBoard, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        try {
            query = "Select count(*) from Assignboard where candidateId = ? and recruiterEmail = ? and status = 'rejected' and deleted = 0";
            int count = jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getCandidateId(), currentUser);
            if(count == 0) {
                query = "select name from MembersProfile where emailId=? and position=?";
                jdbcTemplate.queryForObject(query, String.class, assignBoard.getOrganizerEmail(), "ORGANIZER");
                query = "select candidateId from Assignboard where recruiterEmail=? and candidateId=?";
                jdbcTemplate.queryForObject(query, Integer.class, currentUser, assignBoard.getCandidateId());
                try {
                    query = "update Assignboard set organizerEmail =?, assignDate=curDate(), status = 'NEW' where recruiterEmail=? and candidateId=?";
                    jdbcTemplate.update(query, assignBoard.getOrganizerEmail(), currentUser, assignBoard.getCandidateId());
                    return "Candidate assigned successfully";
                } catch (Exception e) {
                    return "Give correct information";
                }
            }
            else{
                query = "update Assignboard set status='NEW' where candidateId = ? and recruiterEmail = ? and status = 'REJECTED' and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getCandidateId(), currentUser);
                return "Updated";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Select correct Recruiter/Organizer to assign";
        }
    }

    public List<?> getAssignBoardPage(int pageNo, int limit, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct Applications.candidateId) from MembersProfile inner join Assignboard on MembersProfile.emailId=organizerEmail inner join Applications on AssignBoard.candidateId=Applications.candidateId inner join CandidatesProfile on CandidatesProfile.candidateId=applications.candidateId where recruiterEmail=? and status = 'NEW' and MembersProfile.deleted = 0 and CandidatesProfile.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, currentUser);
        }
        query = "select CandidatesProfile.candidateId,CandidatesProfile.name,Applications.designation,Applications.location,AssignBoard.assignDate,MembersProfile.name as organizer  from MembersProfile inner join Assignboard on MembersProfile.emailId=organizerEmail inner join Applications on AssignBoard.candidateId=Applications.candidateId inner join CandidatesProfile on CandidatesProfile.candidateId=applications.candidateId where recruiterEmail=? and status = 'NEW' and MembersProfile.deleted = 0 and CandidatesProfile.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0 group by Applications.candidateId limit ?, ?";
        List<AssignBoardPage> assignBoardPages = jdbcTemplate.query(query,new BeanPropertyRowMapper<>(AssignBoardPage.class), currentUser, offset, limit);

        if(pageNo == 1){
            return List.of(totalCount, assignBoardPages.size(), assignBoardPages);
        }
        return List.of(assignBoardPages.size(), assignBoardPages);
    }

    public List<?> getRejectedCvPage(int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct Applications.candidateId) from Documents inner join CandidatesProfile using(candidateId) inner join Applications using(candidateId) inner join Assignboard using(candidateId) where AssignBoard.status=? and Assignboard.recruiterEmail=? and Documents.deleted = 0 and CandidatesProfile.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, "REJECTED", memberService.getUserNameFromRequest(request));
        }

        query = "select Applications.candidateId, name,imageUrl,Applications.location,mobileNumber from Documents inner join CandidatesProfile using(candidateId) inner join Applications using(candidateId) inner join Assignboard using(candidateId) where AssignBoard.status=? and Assignboard.recruiterEmail=? and Documents.deleted = 0 and CandidatesProfile.deleted = 0 and Assignboard.deleted = 0 and Applications.deleted = 0 group by Applications.candidateId limit ?,?";
        List<RejectedCv> rejectedCvList = new ArrayList<>();
        try {
            jdbcTemplate.query(query,
                    (resultSet,no) -> {
                        RejectedCv list = new RejectedCv();

                        list.setApplicationId(resultSet.getInt(1));
                        list.setName(resultSet.getString(2));
                        list.setImageUrl(resultSet.getString(3));
                        list.setDesignation(getLastJobPosition(list.getApplicationId()));
                        list.setLocation(resultSet.getString(4));
                        list.setMobileNumber(resultSet.getLong(5));
                        rejectedCvList.add(list);
                        return list;
                    },"REJECTED",memberService.getUserNameFromRequest(request), offset, limit);

            if(pageNo == 1){
                return List.of(totalCount, rejectedCvList.size(), rejectedCvList);
            }
            return List.of(rejectedCvList.size(), rejectedCvList);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Invite getInviteInfo(HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);

        Invite invite=new Invite();
        try {
            query = "select count(*) from CandidatesInvites where date=curDate() and fromEmail=? and deleted = 0";
            invite.setToday(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from CandidatesInvites where date=DATE_SUB(curDATE(),INTERVAL 1 DAY) and fromEmail=? and deleted = 0";
            invite.setYesterday(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from CandidatesInvites where month(date)=month(curDate())-1 and year(date)=year(curDate()) and fromEmail=? and deleted = 0";
            invite.setPastMonth(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from CandidatesInvites where month(date)=month(curDate())-2 and year(date)=year(curDate()) and fromEmail=? and deleted = 0";
            invite.setTwoMonthBack(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from CandidatesInvites where year(date)=year(curDate())-1 and fromEmail=? and deleted = 0";
            invite.setPastYear(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from CandidatesInvites where year(date)=year(curDate())-2 and fromEmail=? and deleted = 0";
            invite.setTwoYearBack(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            return invite;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<?> getByDay(Date date, int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from CandidatesInvites where date=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date, memberService.getUserNameFromRequest(request));
        }
        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from CandidatesInvites where date=? and fromEmail=? and deleted = 0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class),date,memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return List.of(totalCount, sentInvites.size(), sentInvites);
        }
        return List.of(sentInvites.size(), sentInvites);

    }

    public List<?> getByMonth(Date date, int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from CandidatesInvites where month(date)=? and year(date)=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request) );
        }
        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from CandidatesInvites where month(date)=? and year(date)=? and fromEmail=? and deleted =0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class), date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return List.of(totalCount, sentInvites.size(), sentInvites);
        }
        return List.of(sentInvites.size(), sentInvites);

    }

    public List<?> getByYear(Date date, int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from CandidatesInvites where year(date)=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request));
        }

        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from CandidatesInvites where year(date)=? and fromEmail=? and deleted = 0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class),date.toLocalDate().getYear(),memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return List.of(totalCount, sentInvites.size(), sentInvites);
        }
        return List.of(sentInvites.size(), sentInvites);

       }
}
