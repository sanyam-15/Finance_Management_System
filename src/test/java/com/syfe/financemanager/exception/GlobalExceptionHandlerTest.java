package com.syfe.financemanager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void notFound() {
        ResponseEntity<?> response = handler.handleResourceNotFoundException(new ResourceNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void badRequest() {
        ResponseEntity<?> response = handler.handleBadRequestException(new BadRequestException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void conflict() {
        ResponseEntity<?> response = handler.handleConflictException(new ConflictException("dup"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void forbidden() {
        ResponseEntity<?> response = handler.handleForbiddenException(new ForbiddenException("no"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void validation() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "must not be blank"));
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<?> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", ((Map<?, ?>) response.getBody()).get("username"));
    }

    @Test
    void badCredentials() {
        ResponseEntity<?> response = handler.handleBadCredentialsException(new BadCredentialsException("fail"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void dateParse() {
        ResponseEntity<?> response = handler.handleDateTimeParseException(new DateTimeParseException("bad date", "x", 0));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void dateTime() {
        ResponseEntity<?> response = handler.handleDateTimeException(new DateTimeException("invalid month"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void fallback() {
        ResponseEntity<?> response = handler.handleGlobalException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((Map<?, ?>) response.getBody()).get("message").toString().contains("boom"));
    }
}
