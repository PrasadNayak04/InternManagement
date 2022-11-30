package com.robosoft.internmanagement.service;

import com.robosoft.internmanagement.constants.AppConstants;
import com.robosoft.internmanagement.exception.FileEmptyException;
import com.robosoft.internmanagement.exception.ResponseData;
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

    public ResponseData<?> candidateRegister(CandidateProfile candidateProfile, HttpServletRequest request) throws Exception {

        if(!isVacantPositionLocation(candidateProfile.getPosition(), candidateProfile.getJobLocation())){
            return new ResponseData<>("FAILED", AppConstants.REQUIREMENTS_FAILED);
        }

        if (alreadyShortlisted(candidateProfile.getEmailId()))
            return new ResponseData<>("FAILED", AppConstants.RECORD_ALREADY_EXIST);

        int candidateId = 0;
        String photoRes = "", resumeRes = "";

        try {
            photoRes = storageService.singleFileUpload(candidateProfile.getPhoto(), candidateProfile.getEmailId(), request, "CANDIDATE");
            resumeRes = storageService.singleFileUpload(candidateProfile.getAttachment(), candidateProfile.getEmailId(), request, "CANDIDATE");

            if (photoRes.equals("empty") || resumeRes.equals("empty") || photoRes.equals("") || resumeRes.equals(""))
                throw new Exception("File not found");

            String query1 = "insert into candidatesprofile(name,dob,mobileNumber,emailId,jobLocation,gender,position,expYear,expMonth,candidateType,contactPerson,languagesKnown,softwaresWorked,skills,about,currentCTC,expectedCTC) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(query1, candidateProfile.getName(), candidateProfile.getDob(), candidateProfile.getMobileNumber(), candidateProfile.getEmailId(), candidateProfile.getJobLocation(), candidateProfile.getGender(), candidateProfile.getPosition(), candidateProfile.getExpYear(), candidateProfile.getExpMonth(), candidateProfile.getCandidateType(), candidateProfile.getContactPerson(), candidateProfile.getLanguagesKnown(), candidateProfile.getSoftwareWorked(), candidateProfile.getSkills(), candidateProfile.getAbout(), candidateProfile.getCurrentCTC(), candidateProfile.getExpectedCTC());

            candidateId = getCandidateId(candidateProfile.getEmailId());

            String documentUrlQuery = "insert into documents(candidateId,attachmentUrl,imageUrl) values(?,?,?)";
            jdbcTemplate.update(documentUrlQuery, candidateId, resumeRes, photoRes);

            List<Education> educations = candidateProfile.getEducations();
            int size = educations.size();
            for (int i = 0; i < size; i++) {
                String educationQuery = "insert into educations(candidateId,institution,grade,fromDate,toDate,location) values(?,?,?,?,?,?)";
                jdbcTemplate.update(educationQuery, candidateId, educations.get(i).getInstitution(), educations.get(i).getGrade(), educations.get(i).getFromDate(), educations.get(i).getToDate(), educations.get(i).getLocation());
            }

            List<WorkHistory> workHistories = candidateProfile.getWorkHistories();
            int workSize = workHistories.size();
            for (int i = 0; i < workSize; i++) {
                String workHistoryQuery = "insert into workhistories(candidateId,company,position,fromDate,toDate,location) values(?,?,?,?,?,?)";
                jdbcTemplate.update(workHistoryQuery, candidateId, workHistories.get(i).getCompany(), workHistories.get(i).getPosition(), workHistories.get(i).getFromDate(), workHistories.get(i).getToDate(), workHistories.get(i).getLocation());
            }

            Address address = candidateProfile.getAddress();
            String addressQuery = "insert into address(candidateId,content,state,pinCode) values(?,?,?,?)";
            jdbcTemplate.update(addressQuery, candidateId, address.getContent(), address.getState(), address.getPinCode());

            List<Link> links = candidateProfile.getLinks();
            int linkSize = links.size();
            for (int i = 0; i < linkSize; i++) {
                String linksQuery = "insert into links(candidateId,url,website) values(?,?,?)";
                jdbcTemplate.update(linksQuery, candidateId, links.get(i).getUrl(), links.get(i).getWebsite());
            }

            LocalDate date = LocalDate.now();
            String insertToApplication = "insert into applications(candidateId,designation,location,date) values(?,?,?,?)";
            jdbcTemplate.update(insertToApplication,candidateId,candidateProfile.getPosition(),candidateProfile.getJobLocation(),date);

            return new ResponseData<>("SUCCESS", AppConstants.SUCCESS);

        } catch (Exception e) {
            delCandidateQuery(candidateId);
            if (photoRes.equals("empty") || resumeRes.equals("empty") || photoRes.equals("") || resumeRes.equals(""))
                throw new FileEmptyException(AppConstants.REQUIREMENTS_FAILED);
            else
                return new ResponseData<>("FAILED", AppConstants.REQUIREMENTS_FAILED);
        }
    }

    public int getCandidateId(String candidateEmail){
        String query = "select max(candidateId) from candidatesprofile where emailId = ? and deleted = 0";
        return jdbcTemplate.queryForObject(query, Integer.class, candidateEmail);
    }

    public void delCandidateQuery(int candidateId) {
        String delQuery = "delete from candidatesprofile where candidateId = ?";
        jdbcTemplate.update(delQuery, candidateId);
    }

    public boolean isVacantPosition(String position){
        String query = "select status from technologies where designation = ? and deleted = 0";
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

    public boolean isVacantPositionLocation(String position, String location){
        try {
            String query = "select vacancy from locations where designation = ? and location =  ? and deleted = 0";
            int vacancy = jdbcTemplate.queryForObject(query, Integer.class, position, location);

            if(isVacantPosition(position) && vacancy > 0)
            {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean alreadyShortlisted(String emailId){
        String query ="select count(candidateId) from assignboard inner join candidatesprofile using(candidateId) where emailId = ? and status = 'SHORTLISTED' and assignboard.deleted = 0 and candidatesprofile.deleted = 0";
        try{
            int status =  jdbcTemplate.queryForObject(query, Integer.class, emailId);
            return status > 0;
        }catch (Exception e){
            return false;
        }
    }

}
