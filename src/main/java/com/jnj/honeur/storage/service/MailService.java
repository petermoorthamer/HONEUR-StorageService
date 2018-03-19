package com.jnj.honeur.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 *
 * @author Peter Moorthamer
 */

@Service
public class MailService {

    private JavaMailSender mailSender;
    private SimpleMailMessage newNotebookMailMessage;

    public MailService(@Autowired JavaMailSender mailSender, @Autowired SimpleMailMessage newNotebookMailMessage) {
        this.mailSender = mailSender;
        this.newNotebookMailMessage = newNotebookMailMessage;
    }

    public void sendNewNotebookMail(String notebookUrl) {
        String text = String.format("Dear HONEUR partner, \n A new notebook is available for download @ %s \n", notebookUrl);
        newNotebookMailMessage.setText(text);
        mailSender.send(newNotebookMailMessage);
    }
}
