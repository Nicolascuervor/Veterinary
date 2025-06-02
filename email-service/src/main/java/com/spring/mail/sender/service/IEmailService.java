package com.spring.mail.sender.service;

import java.io.File;
import java.util.Map;

public interface IEmailService {

    void sendEmail(String[] toUser, String subject, String message);

    void sendEmailWithFile(String[] toUser, String subject, String message, File file);

    void sendEmailTemplate(String[] toUser, String subject, String templateName, Map<String, Object> model);


}
