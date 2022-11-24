package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.model.*;
import com.robosoft.internmanagement.modelAttributes.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecruiterService implements RecruiterServices
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MemberService memberService;

    @Autowired
    private CandidateService candidateService;

    String query;

    public List<?> getAllOrganizers(){
        query = "select emailId, name, photoUrl from membersprofile where position = 'ORGANIZER'";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(MemberModel.class));
    }

    public List<?> getOrganizer(Integer limit, HttpServletRequest request)
    {
        String status = "NEW";
        List<Organizer> organizerList = new ArrayList<>();
        query = "select name, emailId, photoUrl from membersprofile inner join assignboard on emailId = organizerEmail where recruiterEmail = ? and status=? group by organizerEmail";
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

            if (limit == null) {
                limit = organizerList.size();
            }
            if (organizerList.size() >= limit)
                return organizerList.subList(0, limit);
            return Arrays.asList(organizerList.subList(0, organizerList.size()));
    }

    public int getInterviewsCount(String organizerEmail, String recruiterEmail){
        try {
            query = "select count(*) from assignboard where organizerEmail = ? and recruiterEmail = ?";
            return jdbcTemplate.queryForObject(query, Integer.class, organizerEmail, recruiterEmail);
        } catch (Exception e) {
            return 0;
        }
    }

    public Summary getSummary(Date date, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        try {
            Summary summary = new Summary();
            query = "select count(*) from assignboard where month(assignDate)=? and year(assignDate)= ? and status = ? and recruiterEmail=?";
            int shortlisted = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), "SHORTLISTED", currentUser);
            summary.setShortlisted(shortlisted);
            query = "select count(*) from assignboard where month(assignDate)=? and year(assignDate)=? and status=? and recruiterEmail=?";
            int onHold = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), "NEW", currentUser);
            summary.setOnHold(onHold);
            query = "select count(*) from assignboard where month(assignDate)=? and year(assignDate)=? and status=? and recruiterEmail=?";
            int rejected = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), "REJECTED", currentUser);
            summary.setRejected(rejected);
            int assigned = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), "ASSIGNED", currentUser);
            int applications = shortlisted + onHold + rejected + assigned;
            summary.setApplications(applications);
            return summary;
        } catch (Exception e) {
            return new Summary(0,0,0,0);
        }
    }

    public int cvCount(HttpServletRequest request)
    {
        try {
            query = "select count(candidateId) from assignboard where recruiterEmail=? and organizerEmail is null and deleted = 0";
            return jdbcTemplate.queryForObject(query, Integer.class, memberService.getUserNameFromRequest(request));
        } catch (Exception e) {
            return 0;
        }
    }

    public List<?> cvAnalysisPage(Date date, int pageNo, int limit)
    {
        List<CvAnalysis> cvAnalysisList = new ArrayList<>();
        if(date == null){
            date = Date.valueOf(LocalDate.now());
        }

        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        try {
            if (pageNo == 1) {
                query = "select count(distinct applications.designation) from applications,technologies where applications.designation = technologies.designation and date=? and applications.deleted = 0 and technologies.deleted = 0 group by applications.designation";
                totalCount = jdbcTemplate.queryForObject(query, Integer.class, date);
            }

            query = "select applications.designation,count(applications.designation),date,status from applications,technologies where applications.designation = technologies.designation and date=? and applications.deleted = 0 and technologies.deleted = 0 group by applications.designation limit ?, ?";
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

            if (pageNo == 1) {
                return Arrays.asList(totalCount, cvAnalyses.size(), cvAnalyses);
            }
            return Arrays.asList(cvAnalyses.size(), cvAnalyses);
        }catch (Exception e) {
            return Arrays.asList();
        }

    }

    public CvAnalysis searchDesignation(String designation)
    {
        query  = "select applications.designation,count(applications.designation),date,status from applications inner join technologies using(designation) where applications.designation=? and applications.deleted = 0 and technologies.deleted = 0 group by technologies.designation";
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
        query = "update technologies set status = ? where designation = ? and deleted = 0";
        return jdbcTemplate.update(query, newStatus, designation);
    }

    public List<String> getLocationsByDesignation(String designation){
        query = "select location from locations where designation = ? and deleted = 0";
        return jdbcTemplate.queryForList(query, String.class, designation);
    }

    public ExtendedCV getBasicCVDetails(int candidateId, HttpServletRequest request){
        try{
            query = "select " + candidateId + " as candidateId, name, dob, mobileNumber,emailId, jobLocation, position, expYear, expMonth, candidateType, contactPerson, languagesKnown, softwaresWorked, skills, about, expectedCTC, attachmentUrl, imageUrl from candidatesprofile inner join documents using(candidateId) inner join assignboard using(candidateId)  where recruiterEmail= ? and candidatesprofile.candidateId = ? and candidatesprofile.deleted = 0 and documents.deleted = 0 and assignboard.deleted = 0";
            return jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(ExtendedCV.class), memberService.getUserNameFromRequest(request), candidateId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Education> getEducationsHistory(int candidateId){
        query = "select * from educations where candidateId = ? and deleted = 0";
        try {
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Education.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<WorkHistory> getWorkHistory(int candidateId){
        query = "select * from workhistories where candidateId = ? and deleted = 0";
        try{
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(WorkHistory.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Link> getSocialLinks(int candidateId){
        query = "select * from links  where candidateId = ? and deleted = 0";
        try{
            return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Link.class), candidateId);
        } catch (Exception e) {
            return null;
        }
    }

    public String downloadCV(int candidateId, HttpServletRequest request){
        query = "select attachmentUrl from documents inner join assignboard using(candidateId) where candidateId = ? and recruiterEmail = ? and documents.deleted = 0 and assignboard.deleted = 0";
        try{
            return jdbcTemplate.queryForObject(query, String.class, candidateId, memberService.getUserNameFromRequest(request));
        } catch (Exception e) {
            return "";
        }
    }

    public List<TopTechnology> getTopTechnologies(String designation) {
        try {
            List<TopTechnology> topTechnologies = new ArrayList<>();
            query = "select technologies.designation from technologies left join applications using(designation) where applications.designation != ? and applications.deleted = 0 group by technologies.designation order by count(applications.designation) desc limit 5";
            List<String> topTechnologiesNames = jdbcTemplate.queryForList(query, String.class, designation);
            topTechnologies.addAll(setTopTechnologyLocations(topTechnologiesNames));
            System.out.println(topTechnologies.size());
           if (topTechnologies.size() < 5) {
               String parameter = "'...'";
               int required = 5;
               if(topTechnologies.size() > 0) {
                   String parameter2 = "";
                    required = 5 - topTechnologies.size();
                   List<String> tech = topTechnologies.stream().map(e -> e.getDesignation()).collect(Collectors.toList());
                   for (String i : tech)
                       parameter2 += "'" + i + "',";
                   parameter = parameter2.substring(0,parameter.length()-1);
               }

               System.out.println(parameter);
                topTechnologiesNames.clear();
                query = "select technologies.designation from technologies where technologies.designation not in (" + parameter + ") and technologies.designation != ? and technologies.deleted = 0 limit ?";
                topTechnologiesNames.addAll(jdbcTemplate.queryForList(query, String.class,designation, required));
                topTechnologies.addAll(setTopTechnologyLocations(topTechnologiesNames));
            }
            List<String> locations = getLocationsByDesignation(designation);
            TopTechnology technologies = new TopTechnology(designation, locations);
            topTechnologies.add(0, technologies);
            return topTechnologies;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<TopTechnology> setTopTechnologyLocations(List<String> technologyNames){
        try {
            System.out.println(technologyNames.size());
            List<TopTechnology> topTechnologies = new ArrayList<>();
            for (String technology : technologyNames) {
                List<String> locations = getLocationsByDesignation(technology);
                TopTechnology technologies = new TopTechnology(technology, locations);
                topTechnologies.add(technologies);
            }
            return topTechnologies;
        } catch (Exception e) {
            return Arrays.asList();
        }
    }

    public String getLastJobPosition(int candidateId) {
        query = "select position from workhistories where candidateId = ? order by fromDate desc limit 1";
        try {
            return jdbcTemplate.queryForObject(query, String.class, candidateId);
        } catch (Exception e) {
            return null;
        }

    }

    public List<?> getProfileBasedOnStatus(String designation, String status, int pageNo, int limit, HttpServletRequest request) {

        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct applications.candidateId) from candidatesprofile inner join documents using(candidateId) inner join applications using(candidateId) inner join assignboard using(candidateId) where recruiterEmail = ? and assignboard.status = ? and applications.designation = ? and candidatesprofile.deleted = 0 and documents.deleted = 0 and assignboard.deleted = 0 and applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, memberService.getUserNameFromRequest(request), status, designation);
        }

        query = "select candidatesprofile.candidateId, name, imageUrl, skills, position from candidatesprofile inner join documents using(candidateId) inner join applications using(candidateId) inner join assignboard using(candidateId) where recruiterEmail = ? and assignboard.status = ? and applications.designation = ? and candidatesprofile.deleted = 0 and documents.deleted = 0 and assignboard.deleted = 0 and applications.deleted = 0 group by applications.applicationId limit ?, ?";
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
                return Arrays.asList(totalCount, profileAnalyses.size(), profileAnalyses);
            }
            return Arrays.asList(profileAnalyses.size(), profileAnalyses);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public List<Application> getNotAssignedApplicants(HttpServletRequest request)
    {
        query = "select assignboard.candidateId, imageUrl,emailId,mobileNumber,designation,location,date from assignboard inner join applications using(candidateId) inner join candidatesprofile using(candidateId) inner join documents using(candidateId) where organizerEmail is null and recruiterEmail = ? and applications.deleted = 0 and assignboard.deleted = 0 and candidatesprofile.deleted = 0 and documents.deleted = 0";
        return jdbcTemplate.query(query,new BeanPropertyRowMapper<>(Application.class), memberService.getUserNameFromRequest(request));
    }

    public String assignOrganizer(AssignBoard assignBoard, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        try {
            if (!candidateService.isVacantPosition(memberService.getAssignBoardPageDetails(assignBoard).getDesignation())){
                return "Designation status closed. No vacancy";
            }
            query = "Select count(*) from assignboard where candidateId = ? and recruiterEmail = ? and status = 'rejected' and deleted = 0";
            int count = jdbcTemplate.queryForObject(query, Integer.class, assignBoard.getCandidateId(), currentUser);
            if(count == 0) {
                query = "select name from membersprofile where emailId=? and position=?";
                jdbcTemplate.queryForObject(query, String.class, assignBoard.getOrganizerEmail(), "ORGANIZER");
                query = "select candidateId from assignboard where recruiterEmail=? and candidateId=?";
                jdbcTemplate.queryForObject(query, Integer.class, currentUser, assignBoard.getCandidateId());
                try {
                    query = "update assignboard set organizerEmail =?, assignDate=curDate(), status = 'NEW' where recruiterEmail=? and candidateId=?";
                    jdbcTemplate.update(query, assignBoard.getOrganizerEmail(), currentUser, assignBoard.getCandidateId());
                    return "Candidate assigned successfully";
                } catch (Exception e) {
                    return "Give correct information";
                }
            }
            else{
                query = "update assignboard set status='NEW' where candidateId = ? and recruiterEmail = ? and status = 'REJECTED' and deleted = 0";
                jdbcTemplate.update(query, assignBoard.getCandidateId(), currentUser);
                return "Updated";
            }
        } catch (Exception e) {
            return "Select correct Recruiter/Organizer/Candidate to assign";
        }
    }

    public boolean rejectAssignedCandidate(int candidateId, HttpServletRequest request){
        String query = "update assignboard set status='REJECTED' where candidateId=? and recruiterEmail=? and status=? and deleted = 0";
        int status = jdbcTemplate.update(query,candidateId, memberService.getUserNameFromRequest(request),"ASSIGNED");
        return status >= 1;
    }

    public boolean reRecruitCandidate(int candidateId, HttpServletRequest request){
        String query = "update assignboard set status='ASSIGNED', organizerEmail = null where candidateId=? and recruiterEmail=? and status=? and deleted = 0";
        int status = jdbcTemplate.update(query,candidateId, memberService.getUserNameFromRequest(request),"REJECTED");
        return status >= 1;
    }

    public boolean deleteCandidate(int candidateId, HttpServletRequest request){
        query = "select count(*) from candidatesprofile where candidateId = ? and deleted = 1";
        if(jdbcTemplate.queryForObject(query, Integer.class, candidateId) > 0)
            return false;
        String query = "update candidatesprofile, documents, educations, workhistories, links, applications, assignboard set candidatesprofile.deleted=1, educations.deleted=1, workhistories.deleted=1, documents.deleted=1, links.deleted=1, applications.deleted=1, assignboard.deleted=1 where candidatesprofile.candidateId=? and documents.candidateId=? and educations.candidateId=? and workhistories.candidateId=? and links.candidateId=? and applications.candidateId=? and assignboard.candidateId=? and recruiterEmail=? and assignboard.status = 'REJECTED'";
        int status = jdbcTemplate.update(query,candidateId, candidateId, candidateId, candidateId, candidateId, candidateId, candidateId, memberService.getUserNameFromRequest(request));
        query = "update results, assignboard set results.deleted = 1 where results.candidateId=? and assignboard.recruiterEmail=? and results.candidateId = assignboard.candidateId";
        jdbcTemplate.update(query, candidateId, memberService.getUserNameFromRequest(request));
        return status >= 1;
    }

    public List<?> getAssignBoardPage(int pageNo, int limit, HttpServletRequest request)
    {
        String currentUser = memberService.getUserNameFromRequest(request);
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct applications.candidateId) from membersprofile inner join assignboard on membersprofile.emailId=organizerEmail inner join applications on AssignBoard.candidateId=applications.candidateId inner join candidatesprofile on candidatesprofile.candidateId=applications.candidateId where recruiterEmail=? and status = 'NEW' and membersprofile.deleted = 0 and candidatesprofile.deleted = 0 and Assignboard.deleted = 0 and applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, currentUser);
        }
        query = "select candidatesprofile.candidateId,candidatesprofile.name,applications.designation,applications.location,AssignBoard.assignDate,membersprofile.name as organizer  from membersprofile inner join Assignboard on membersprofile.emailId=organizerEmail inner join applications on AssignBoard.candidateId=applications.candidateId inner join candidatesprofile on candidatesprofile.candidateId=applications.candidateId where recruiterEmail=? and status = 'NEW' and membersprofile.deleted = 0 and candidatesprofile.deleted = 0 and Assignboard.deleted = 0 and applications.deleted = 0 group by applications.candidateId limit ?, ?";
        List<AssignBoardPage> assignBoardPages = jdbcTemplate.query(query,new BeanPropertyRowMapper<>(AssignBoardPage.class), currentUser, offset, limit);

        if(pageNo == 1){
            return Arrays.asList(totalCount, assignBoardPages.size(), assignBoardPages);
        }
        return Arrays.asList(assignBoardPages.size(), assignBoardPages);
    }

    public List<?> getRejectedCvPage(int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(distinct applications.candidateId) from documents inner join candidatesprofile using(candidateId) inner join applications using(candidateId) inner join Assignboard using(candidateId) where AssignBoard.status=? and Assignboard.recruiterEmail=? and documents.deleted = 0 and candidatesprofile.deleted = 0 and Assignboard.deleted = 0 and applications.deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, "REJECTED", memberService.getUserNameFromRequest(request));
        }

        query = "select applications.candidateId, name,imageUrl,applications.location,mobileNumber from documents inner join candidatesprofile using(candidateId) inner join applications using(candidateId) inner join Assignboard using(candidateId) where AssignBoard.status=? and Assignboard.recruiterEmail=? and documents.deleted = 0 and candidatesprofile.deleted = 0 and Assignboard.deleted = 0 and applications.deleted = 0 group by applications.candidateId limit ?,?";
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
                return Arrays.asList(totalCount, rejectedCvList.size(), rejectedCvList);
            }
            return Arrays.asList(rejectedCvList.size(), rejectedCvList);
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
            query = "select count(*) from candidatesinvites where date=curDate() and fromEmail=? and deleted = 0";
            invite.setToday(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from candidatesinvites where date=DATE_SUB(curDATE(),INTERVAL 1 DAY) and fromEmail=? and deleted = 0";
            invite.setYesterday(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from candidatesinvites where month(date)=month(curDate())-1 and year(date)=year(curDate()) and fromEmail=? and deleted = 0";
            invite.setPastMonth(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from candidatesinvites where month(date)=month(curDate())-2 and year(date)=year(curDate()) and fromEmail=? and deleted = 0";
            invite.setTwoMonthBack(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from candidatesinvites where year(date)=year(curDate())-1 and fromEmail=? and deleted = 0";
            invite.setPastYear(jdbcTemplate.queryForObject(query, Integer.class, currentUser));

            query = "select count(*) from candidatesinvites where year(date)=year(curDate())-2 and fromEmail=? and deleted = 0";
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
            query = "select count(*) from candidatesinvites where date=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date, memberService.getUserNameFromRequest(request));
        }
        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from candidatesinvites where date=? and fromEmail=? and deleted = 0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class),date,memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return Arrays.asList(totalCount, sentInvites.size(), sentInvites);
        }
        return Arrays.asList(sentInvites.size(), sentInvites);

    }

    public List<?> getByMonth(Date date, int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from candidatesinvites where month(date)=? and year(date)=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getMonthValue(), date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request) );
        }
        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from candidatesinvites where month(date)=? and year(date)=? and fromEmail=? and deleted =0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class), date.toLocalDate().getMonthValue(),date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return Arrays.asList(totalCount, sentInvites.size(), sentInvites);
        }
        return Arrays.asList(sentInvites.size(), sentInvites);

    }

    public List<?> getByYear(Date date, int pageNo, int limit, HttpServletRequest request)
    {
        int offset = (pageNo - 1) * limit;
        int totalCount = 0;
        if(pageNo == 1){
            query = "select count(*) from candidatesinvites where year(date)=? and fromEmail=? and deleted = 0";
            totalCount = jdbcTemplate.queryForObject(query, Integer.class, date.toLocalDate().getYear(), memberService.getUserNameFromRequest(request));
        }

        query = "select candidateInviteId, candidateName as name,designation,location,CandidateEmail as email from candidatesinvites where year(date)=? and fromEmail=? and deleted = 0 limit ?, ?";
        List<SentInvite> sentInvites = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(SentInvite.class),date.toLocalDate().getYear(),memberService.getUserNameFromRequest(request), offset, limit);

        if(pageNo == 1){
            return Arrays.asList(totalCount, sentInvites.size(), sentInvites);
        }
        return Arrays.asList(sentInvites.size(), sentInvites);

       }
}
