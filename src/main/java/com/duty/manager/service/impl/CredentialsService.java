package com.duty.manager.service.impl;

import com.duty.manager.service.UserDetailsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CredentialsService implements UserDetailsService {

    private final List<UserDetailsProvider<? extends UserDetails>> userDetailsProviders;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<? extends UserDetails> userDetails = userDetailsProviders.stream()
                .map(p -> p.getUserByIdentifier(email))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Email or password are not correct"));
        return userDetails.get();
    }
}
