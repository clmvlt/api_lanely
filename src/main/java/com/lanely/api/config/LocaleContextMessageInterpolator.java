package com.lanely.api.config;

import jakarta.validation.MessageInterpolator;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class LocaleContextMessageInterpolator implements MessageInterpolator {

    private final MessageInterpolator delegate;

    public LocaleContextMessageInterpolator(MessageInterpolator delegate) {
        this.delegate = delegate;
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return this.delegate.interpolate(messageTemplate, context, LocaleContextHolder.getLocale());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return this.delegate.interpolate(messageTemplate, context, locale);
    }
}
