package com.mycom.myapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcsConfig {

    @Value("${gcp.credentials.location}")
    private String credentialsPath;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    @Bean
    public Storage storage() throws IOException {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}
