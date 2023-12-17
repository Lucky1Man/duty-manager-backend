package com.duty.manager.config;

import com.duty.manager.entity.Participant;
import com.duty.manager.service.UserDetailsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StaticUserProvider implements UserDetailsProvider<UserDetails> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserDetails> getUserByIdentifier(String identifier) {
        return Optional.of(
                Participant.builder()
                        .withFullName("Orest")
                        .withEmail("admin@gmail.com")
                        .withPassword(passwordEncoder.encode("Qq111111"))
                        .build()
        );
    }
}
