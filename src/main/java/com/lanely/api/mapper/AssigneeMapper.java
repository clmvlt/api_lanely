package com.lanely.api.mapper;

import com.lanely.api.entity.Account;
import com.lanely.api.entity.enums.AccountType;
import com.lanely.api.service.StatusHistorySupport;

import java.util.UUID;

public final class AssigneeMapper {

    private AssigneeMapper() {
    }

    public static UUID id(Account account) {
        return account == null ? null : account.getId();
    }

    public static AccountType type(Account account) {
        return account == null ? null : StatusHistorySupport.actorType(account);
    }

    public static String name(Account account) {
        return account == null ? null : StatusHistorySupport.actorName(account);
    }
}
