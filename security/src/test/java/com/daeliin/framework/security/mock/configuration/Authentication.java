package com.daeliin.framework.security.mock.configuration;

import com.daeliin.framework.security.authentication.form.FormAuthentication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@PropertySource("classpath:security.properties")
public class Authentication extends FormAuthentication {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        
        http
            .csrf()
            .disable();
    }
}