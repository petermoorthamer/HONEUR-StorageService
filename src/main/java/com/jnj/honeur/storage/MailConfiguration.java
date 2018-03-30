package com.jnj.honeur.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Spring configuration for sending emails
 * @author Peter Moorthamer
 */

@Configuration
public class MailConfiguration {

    @Bean
    public JavaMailSender getJavaMailSender(
            @Value("${mail.server.host}") String mailServerHost,
            @Value("${mail.server.port}") int mailServerPort,
            @Value("${mail.server.username}") String mailServerUsername,
            @Value("${mail.server.password}") String mailServerPassword  ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);
        mailSender.setUsername(mailServerUsername);
        mailSender.setPassword(mailServerPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public SimpleMailMessage newNotebookMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("HONEUR@its.jnj.com");
        message.setTo("moorthamer@gmail.com");
        message.setSubject("New notebook available for download!");
        message.setText("Dear HONEUR partner, a new notebook is available for download @ %s \n");
        return message;
    }

}
