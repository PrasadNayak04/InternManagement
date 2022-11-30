package com.robosoft.internmanagement.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService implements StorageServices
{

    private static final String UPLOADED_FOLDER = "src\\main\\resources\\static\\documents\\";

    private final Path root = Paths.get("src\\main\\resources\\static\\documents\\");

    public String singleFileUpload(MultipartFile file, String email, HttpServletRequest request, String position) throws Exception {
        String fileUrl = null;

        try {
            if (file.isEmpty() && position.equalsIgnoreCase("MEMBER")) {
                return "http://localhost:8080/intern-management/member/fetch/default@gmail.com/default.png";
            }
            else if (file.isEmpty() && position.equalsIgnoreCase("CANDIDATE")) {
                return "empty";
            }

            File newDirectory = new File(UPLOADED_FOLDER, email);
            if(!(newDirectory.exists())){
                newDirectory.mkdir();
            }
            String CREATED_FOLDER = UPLOADED_FOLDER + email + "\\";
            byte[] bytes = file.getBytes();
            Path path = Paths.get(CREATED_FOLDER  + file.getOriginalFilename());
            System.out.println(path);
            Files.write(path, bytes);
            String fileName = file.getOriginalFilename().replaceAll(" ","-" );
            fileUrl = generateDocumentUrl(email + "/" + fileName);
            System.out.println(fileUrl);

        } catch (Exception i) {
            return "empty";
        }

        return fileUrl;
    }

    public String generateDocumentUrl(String fileName){
        final String apiUrl = "http://localhost:8080/intern-management/member/fetch/";
        return apiUrl + fileName;
    }

    public String getContentType(HttpServletRequest request, Resource resource, String fileName){
        String contentType = null;
        try {
            int length = fileName.length();
            contentType = fileName.substring(length-4,length);
            if(contentType.equalsIgnoreCase("jfif")) {
                return "image/jpeg";
            }
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            System.out.println(contentType+" Type ");
        }
        catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }

        if (contentType == null) {
            System.out.println("Inside null");
            contentType = "application/octet-stream";
        }
        return contentType;
    }

}
