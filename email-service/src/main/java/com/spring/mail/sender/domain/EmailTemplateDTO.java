package com.spring.mail.sender.domain;

import java.util.Map;

public class EmailTemplateDTO {

    private String[] toUser;
    private String subject;
    private String templateName;
    private Map<String, Object> model;

    public EmailTemplateDTO() {}

    public EmailTemplateDTO(String[] toUser, String subject, String templateName, Map<String, Object> model) {
        this.toUser = toUser;
        this.subject = subject;
        this.templateName = templateName;
        this.model = model;
    }

    public String[] getToUser() {
        return toUser;
    }

    public void setToUser(String[] toUser) {
        this.toUser = toUser;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}