package com.lanely.api.mapper;

import com.lanely.api.dto.auth.UserSummary;
import com.lanely.api.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.isEmailVerified(), ImageMapper.url(user.getProfileImage()), subscriptionPlanCode(user));
    }

    public static String subscriptionPlanCode(User user) {
        return user.getSubscriptionPlan() == null ? null : user.getSubscriptionPlan().getCode().name();
    }

    public static String displayName(User user) {
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? user.getEmail() : name;
    }
}
