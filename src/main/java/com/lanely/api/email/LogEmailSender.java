package com.lanely.api.email;

import com.lanely.api.config.EmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "log", matchIfMissing = true)
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    private final EmailProperties properties;

    public LogEmailSender(EmailProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(EmailMessage message) {
        log.info("Email (log provider) from='{}' to='{}' subject='{}' body='{}'",
                properties.from(), message.to(), message.subject(), message.textBody());
    }
}
