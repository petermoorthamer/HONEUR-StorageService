package com.jnj.honeur.storage;

import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.jnj.honeur.aws.s3.AmazonS3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * Spring boot application for launching the HONEUR Storage Service
 *
 * @author Peter Moorthamer
 */

@SpringBootApplication
public class StorageApplication extends SpringBootServletInitializer {

	private static Logger LOG = LoggerFactory.getLogger(StorageApplication.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StorageApplication.class);
    }

    @Bean(name = "amazonS3Service")
    public AmazonS3Service amazonS3Service() {
        final AWSSecurityTokenService tokenService = AWSSecurityTokenServiceClientBuilder.defaultClient();
        return new AmazonS3Service(new STSSessionCredentialsProvider(tokenService));
    }

	public static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

}
