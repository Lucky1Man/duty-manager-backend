package com.duty.manager.config;

import com.duty.manager.controller.ExceptionResponse;
import com.duty.manager.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class RestResponseAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    private final TimeService timeService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        PrintWriter writer = response.getWriter();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        writer.print(objectMapper.writeValueAsString(ExceptionResponse.builder()
                .withMessage("You are not logged in, or you do not have rights to perform this action.")
                .withDate(timeService.utcNow())
                .withHttpStatus(HttpStatus.UNAUTHORIZED)
                .build()));
        writer.flush();
    }
}
