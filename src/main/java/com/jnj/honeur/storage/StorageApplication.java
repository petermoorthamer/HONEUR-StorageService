package com.jnj.honeur.storage;

import com.amazonaws.auth.BasicAWSCredentials;
import com.jnj.honeur.aws.s3.AmazonS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
public class StorageApplication {

	private static Logger LOG = LoggerFactory.getLogger(StorageApplication.class);

    @Bean(name = "amazonS3Service")
    public AmazonS3Service moorthamerAmazonS3Service(@Value("${amazon.s3.accessKey}") String accessKey, @Value("${amazon.s3.secretKey}") String secretKey) {
        return new AmazonS3Service(new BasicAWSCredentials(accessKey, secretKey));
    }

	public static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

}
