package com.duty.manager.service.impl;

import com.duty.manager.entity.Role;
import jakarta.annotation.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Function;

public class AuthenticationAwareService {

    public <T> T avoidingRoleCall(Function<Boolean, T> authenticationAwarefunction, @Nullable Role avoidedRole) {
        boolean needsToBeSecured = avoidedRole != null && SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(avoidedRole.getAuthority()));
        return authenticationAwarefunction.apply(needsToBeSecured);
    }

}
