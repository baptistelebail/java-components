package com.daeliin.framework.core.mail;

import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
public class Mails {
    
    @Value("${mail.domain.name}")
    private String domainName;
    
    @Value("${mail.domain.url}")
    private String domainUrl;
    
    private final TemplateEngine templateEngine;
    private final JavaMailSenderImpl mailSender;

    @Autowired
    public Mails(final TemplateEngine templateEngine, final JavaMailSenderImpl mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }
    
    public void send(Mail mail) {
        MimeMessage message = this.mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(mail.to());
            helper.setSentDate(null);
            helper.setFrom(mail.from());
            helper.setSubject(mail.subject());
            helper.setText(processBody(mail.parameters(), mail.templateName()), true);
            
            this.mailSender.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        } 
    }
    
    private String processBody(Map<String, String> parameters, final String templateName) {
        Context context = new Context();
        
        addGlobalParameters(parameters);
        
        parameters.entrySet().forEach(parameter -> {
            context.setVariable(parameter.getKey(), parameter.getValue());
        });
        
        return this.templateEngine.process(templateName, context);
    }
    
    private void addGlobalParameters(Map<String, String> parameters) {
        parameters.put("domainName", domainName);
        parameters.put("domainUrl", domainUrl);
    }
}