package com.lanely.api.service.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CodeGenerator {

    private static final char[] COMPANY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int COMPANY_CODE_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    public String companyCode() {
        StringBuilder builder = new StringBuilder(COMPANY_CODE_LENGTH);
        for (int i = 0; i < COMPANY_CODE_LENGTH; i++) {
            builder.append(COMPANY_ALPHABET[secureRandom.nextInt(COMPANY_ALPHABET.length)]);
        }
        return builder.toString();
    }

    public String invitationCode() {
        return secureToken();
    }

    public String secureToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
