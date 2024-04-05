package com.duty.manager.service.impl;

import com.duty.manager.entity.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Consumer;
import java.util.function.Function;

public class AuthenticationAwareService {

    public <T> T roleAwareCall(Function<Boolean, T> roleAwareFunction, @Nullable Role role) {
        boolean found = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> {
                            if (role == null) {
                                return true;
                            } else {
                                return authority.getAuthority().equals(role.getAuthority());
                            }
                        }
                );
        return roleAwareFunction.apply(found);
    }

    public Authentication authAwareCall(Consumer<Authentication> authConsumer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authConsumer.accept(authentication);
        return authentication;
    }

    public boolean containsRole(@NotNull Authentication authentication, @Nullable Role role) {
        if (role == null) {
            return true;
        } else {
            return authentication.getAuthorities().stream()
                    .anyMatch(
                            authority -> authority.getAuthority().equals(role.getAuthority())
                    );
        }
    }

}
