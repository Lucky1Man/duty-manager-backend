package com.duty.manager.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.datetime.standard.DateTimeContext;
import org.springframework.format.datetime.standard.DateTimeContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;

@Component
public class TimeZoneFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String timeZoneHeader = request.getHeader("Time-Zone");
        if(timeZoneHeader != null) {
            DateTimeContext dateTimeContext = new DateTimeContext();
            dateTimeContext.setTimeZone(ZoneId.of(timeZoneHeader));
            DateTimeContextHolder.setDateTimeContext(dateTimeContext);
        }
        filterChain.doFilter(request, response);
    }
}
