package com.daeliin.framework.commons.security.membership;

import com.daeliin.framework.commons.security.credentials.account.Account;
import com.daeliin.framework.core.mail.Mail;
import com.daeliin.framework.core.mail.Mails;
import com.daeliin.framework.core.resource.exception.MailBuildingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("mail")
public class MailingMemberShipNotifications implements MembershipNotifications {

    @Value("${mail.from}")
    private String from;
    
    @Autowired
    protected Mails mails;

    @Autowired
    protected MessageSource messages;
    
    @Async
    @Override
    public void signUp(final Account account) {
        Map<String, String> parameters = addAccountParameters(account);
        
        try {
            Mail mail = 
                Mail.builder()
                .from(from)
                .to(account.getEmail())
                .subject(messages.getMessage("membership.mail.signup.subject", null, Locale.getDefault()))
                .templateName("signUp")
                .parameters(parameters)
                .build();
                
                mails.send(mail);
        } catch (MailBuildingException e) {
            log.error(String.format("Sign up mail for account %s was invalid", account), e);
        }
    }

    @Async
    @Override
    public void activate(final Account account) {
        Map<String, String> parameters = addAccountParameters(account);
        
        try {
            Mail mail = 
                Mail.builder()
                .from(from)
                .to(account.getEmail())
                .subject(messages.getMessage("membership.mail.activate.subject", null, Locale.getDefault()))
                .templateName("activate")
                .parameters(parameters)
                .build();
            
            mails.send(mail);
        } catch (MailBuildingException e) {
            log.error(String.format("Activate mail for account %s was invalid", account), e);
        }
    }

    @Async
    @Override
    public void newPassword(final Account account) {
        Map<String, String> parameters = addAccountParameters(account);
        
        try {
            Mail mail = 
                Mail.builder()
                .from(from)
                .to(account.getEmail())
                .subject(messages.getMessage("membership.mail.newPassword.subject", null, Locale.getDefault()))
                .templateName("newPassword")
                .parameters(parameters)
                .build();
            
            mails.send(mail);
        } catch (MailBuildingException e) {
            log.error(String.format("New password mail for account %s was invalid", account), e);
        }
    }

    @Async
    @Override
    public void resetPassword(final Account account) {
        Map<String, String> parameters = addAccountParameters(account);
        
        try {
            Mail mail = 
                Mail.builder()
                .from(from)
                .to(account.getEmail())
                .subject(messages.getMessage("membership.mail.resetPassword.subject", null, Locale.getDefault()))
                .templateName("resetPassword")
                .parameters(parameters)
                .build();
            
            mails.send(mail);
        } catch (MailBuildingException e) {
            log.error(String.format("Reset password mail for account %s was invalid", account), e);
        }
    }
    
    private Map<String, String> addAccountParameters(final Account account) {
        Map<String, String> accountParameters = new HashMap<>();
        
        accountParameters.put("userName", account.getUsername());
        accountParameters.put("userEmail", account.getEmail());
        accountParameters.put("userToken", account.getToken());
            
        return accountParameters;
    }
}
