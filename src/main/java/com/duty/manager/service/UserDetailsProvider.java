package com.duty.manager.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserDetailsProvider <T extends UserDetails> {

    Optional<T> getUserByIdentifier(String identifier);

}
