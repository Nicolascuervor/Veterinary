package com.spring.mail.sender.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailServiceImpl implements IEmailService{

    @Value("${email.sender}")
    private String emailUser;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    TemplateEngine templateEngine;

    @Override
    public void sendEmail(String[] toUser, String subject, String message) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(emailUser);
        mailMessage.setTo(toUser);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    @Override
    public void sendEmailWithFile(String[] toUser, String subject, String message, File file) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            mimeMessageHelper.setFrom(emailUser);
            mimeMessageHelper.setTo(toUser);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(message);
            mimeMessageHelper.addAttachment(file.getName(), file);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailTemplate(String[] toUser, String subject, String templateName, Map<String, Object> model) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            Context context = new Context();

            if (model != null) {
                model.forEach(context::setVariable);
            }

            String htmlText = templateEngine.process(templateName, context); // "email-template" u otro

            helper.setFrom(emailUser);
            helper.setTo(toUser);
            helper.setSubject(subject);
            helper.setText(htmlText, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
