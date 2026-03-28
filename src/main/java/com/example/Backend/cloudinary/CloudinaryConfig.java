package com.example.Backend.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    // Bean cho Account 1 (mặc định)
    @Bean(name = "cloudinaryAccount1")
    public Cloudinary cloudinaryAccount1(
            @Value("${app.cloudinary.account1.cloud-name}") String cloudName,
            @Value("${app.cloudinary.account1.api-key}") String apiKey,
            @Value("${app.cloudinary.account1.api-secret}") String apiSecret) {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // Bean cho Account 2
    @Bean(name = "cloudinaryAccount2")
    public Cloudinary cloudinaryAccount2(
            @Value("${app.cloudinary.account2.cloud-name}") String cloudName,
            @Value("${app.cloudinary.account2.api-key}") String apiKey,
            @Value("${app.cloudinary.account2.api-secret}") String apiSecret) {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}