package com.gateway.authenticationservice.Emails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {
    private String[] toUser;
    private String subject;
    private String message;
}