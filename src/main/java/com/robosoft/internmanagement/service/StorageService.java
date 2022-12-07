package com.robosoft.internmanagement.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class StorageService implements StorageServices
{

    public Map upload(Object file, Map options)
    {
        Cloudinary cloudinary = Singleton.getCloudinary();
        cloudinary.config.cloudName = "de3kkygvy";
        cloudinary.config.apiSecret = "Z1yeTZZR-WguUfCIw110C0YrPJY";
        cloudinary.config.apiKey = "881436721362781";

        try
        {
            return cloudinary.uploader().upload(file, options);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
