package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.modelAttributes.*;
import com.robosoft.internmanagement.service.jwtSecurity.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@Service
public class CandidateService implements CandidateServices
{
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    StorageService storageService;

    @Autowired
    private TokenManager tokenManager;

    public String candidateRegister(CandidateProfile candidateProfile, HttpServletRequest request) throws Exception {

        if(!(isVacantPosition(candidateProfile.getPosition()))){
            return "Application status is closed.";
        }

        int candidateId = 0;

        try {
            String query1 = "insert into CandidatesProfile(name,dob,mobileNumber,emailId,jobLocation,gender,position,expYear,expMonth,candidateType,contactPerson,languagesKnown,softwaresWorked,skills,about,currentCTC,expectedCTC) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(query1, candidateProfile.getName(), candidateProfile.getDob(), candidateProfile.getMobileNumber(), candidateProfile.getEmailId(), candidateProfile.getJobLocation(), candidateProfile.getGender(), candidateProfile.getPosition(), candidateProfile.getExpYear(), candidateProfile.getExpMonth(), candidateProfile.getCandidateType(), candidateProfile.getContactPerson(), candidateProfile.getLanguagesKnown(), candidateProfile.getSoftwareWorked(), candidateProfile.getSkills(), candidateProfile.getAbout(), candidateProfile.getCurrentCTC(), candidateProfile.getExpectedCTC());

            candidateId = getCandidateId(candidateProfile.getEmailId());

            String photoRes = storageService.singleFileUpload(candidateProfile.getPhoto(), candidateProfile.getEmailId(), request, "CANDIDATE");
            if (photoRes.equals("empty")) {
                throw new Exception("File empty");
            }

            String resumeRes = storageService.singleFileUpload(candidateProfile.getAttachment(), candidateProfile.getEmailId(), request, "CANDIDATE");
            if (resumeRes.equals("empty")) {
                throw new Exception("File empty");
            }

            String documentUrlQuery = "insert into Documents(candidateId,attachmentUrl,imageUrl) values(?,?,?)";
            jdbcTemplate.update(documentUrlQuery, candidateId, resumeRes, photoRes);

            List<Education> educations = candidateProfile.getEducations();
            int size = educations.size();
            for (int i = 0; i < size; i++) {
                String educationQuery = "insert into Educations(candidateId,institution,grade,fromDate,toDate,location) values(?,?,?,?,?,?)";
                jdbcTemplate.update(educationQuery, candidateId, educations.get(i).getInstitution(), educations.get(i).getGrade(), educations.get(i).getFromDate(), educations.get(i).getToDate(), educations.get(i).getLocation());
            }

            List<WorkHistory> workHistories = candidateProfile.getWorkHistories();
            int workSize = workHistories.size();
            for (int i = 0; i < workSize; i++) {
                String workHistoryQuery = "insert into WorkHistories(candidateId,company,position,fromDate,toDate,location) values(?,?,?,?,?,?)";
                jdbcTemplate.update(workHistoryQuery, candidateId, workHistories.get(i).getCompany(), workHistories.get(i).getPosition(), workHistories.get(i).getFromDate(), workHistories.get(i).getToDate(), workHistories.get(i).getLocation());
            }

            Address address = candidateProfile.getAddress();
            String addressQuery = "insert into Address(candidateId,content,state,pinCode) values(?,?,?,?)";
            jdbcTemplate.update(addressQuery, candidateId, address.getContent(), address.getState(), address.getPinCode());

            List<Link> links = candidateProfile.getLinks();
            int linkSize = links.size();
            for (int i = 0; i < linkSize; i++) {
                String linksQuery = "insert into Links(candidateId,url,website) values(?,?,?)";
                jdbcTemplate.update(linksQuery, candidateId, links.get(i).getUrl(), links.get(i).getWebsite());
            }

            LocalDate date = LocalDate.now();
            String insertToApplication = "insert into Applications(candidateId,designation,location,date) values(?,?,?,?)";
            jdbcTemplate.update(insertToApplication,candidateId,candidateProfile.getPosition(),candidateProfile.getJobLocation(),date);

        } catch (Exception e) {
            delCandidateQuery(candidateId);
            return "Save failed";
        }

        return "Candidate saved successfully";
    }

    public int getCandidateId(String candidateEmail){
        String query = "select max(candidateId) from CandidatesProfile where emailId = ? and deleted = 0";
        return jdbcTemplate.queryForObject(query, Integer.class, candidateEmail);
    }

    public void delCandidateQuery(int candidateId) {
        System.out.println("deleting " + candidateId);
        String delQuery = "delete from CandidatesProfile where candidateId = ?";
        jdbcTemplate.update(delQuery, candidateId);
    }

    public boolean isVacantPosition(String position){
        String query = "select status from Technologies where designation = ? and deleted = 0";
        try {
            String status = jdbcTemplate.queryForObject(query, String.class, position);
            if(status.equalsIgnoreCase("ACTIVE"))
            {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
