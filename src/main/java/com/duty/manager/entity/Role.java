package com.duty.manager.entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN, PARTICIPANT;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
