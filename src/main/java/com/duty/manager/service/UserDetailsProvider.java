package com.duty.manager.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserDetailsProvider <T extends UserDetails> {

    //TODO refactor this so it returns GetParticipantDTO when security is done
    Optional<T> getUserByIdentifier(String identifier);

}
