package com.jnj.honeur.storage;

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
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("moorthamer@gmail.com");
        mailSender.setPassword("???");

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
