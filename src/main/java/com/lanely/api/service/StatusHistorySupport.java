package com.lanely.api.service;

import com.lanely.api.entity.Account;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.AccountType;

public final class StatusHistorySupport {

    private StatusHistorySupport() {
    }

    public static AccountType actorType(Account actor) {
        if (actor instanceof Profile) {
            return AccountType.PROFILE;
        }
        return AccountType.USER;
    }

    public static String actorName(Account actor) {
        if (actor instanceof User user) {
            String full = ((blank(user.getFirstName()) ? "" : user.getFirstName().trim()) + " "
                    + (blank(user.getLastName()) ? "" : user.getLastName().trim())).trim();
            return full.isEmpty() ? user.getEmail() : full;
        }
        if (actor instanceof Profile profile) {
            return blank(profile.getDisplayName()) ? profile.getUsername() : profile.getDisplayName().trim();
        }
        return "system";
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
