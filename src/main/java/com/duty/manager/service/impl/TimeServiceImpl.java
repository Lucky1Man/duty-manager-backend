package com.duty.manager.service.impl;

import com.duty.manager.service.TimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeServiceImpl implements TimeService {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

}
