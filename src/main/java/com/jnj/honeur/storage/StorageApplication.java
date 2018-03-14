package com.jnj.honeur.storage;

import com.jnj.honeur.aws.s3.AmazonS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring boot application for launching the HONEUR Storage Service
 *
 * @author Peter Moorthamer
 */

@SpringBootApplication
public class StorageApplication {

	private static Logger LOG = LoggerFactory.getLogger(StorageApplication.class);

    @Bean(name = "amazonS3Service")
    public AmazonS3Service amazonS3Service() {
        //return new AmazonS3Service(new BasicAWSCredentials(accessKey, secretKey));
        return new AmazonS3Service();
    }

	public static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

}
