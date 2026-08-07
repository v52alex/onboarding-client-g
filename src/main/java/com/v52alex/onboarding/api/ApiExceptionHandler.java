package com.v52alex.onboarding.api;

import com.v52alex.onboarding.application.CaseNotFoundException;
import com.v52alex.onboarding.application.InvalidTransitionException;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.application.DocumentValidationException;
import com.v52alex.onboarding.application.InvalidReviewTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CaseNotFoundException.class)
    ProblemDetail notFound(CaseNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidTransitionException.class)
    ProblemDetail conflict(InvalidTransitionException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidReviewTransitionException.class)
    ProblemDetail invalidReview(InvalidReviewTransitionException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidActionPayloadException.class)
    ProblemDetail invalidActionPayload(InvalidActionPayloadException exception,
        HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidRequest(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Request validation failed", request);
        detail.setProperty("errors", exception.getFieldErrors().stream()
            .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
            .toList());
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalidConstraint(ConstraintViolationException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Request constraint validation failed", request);
    }

    @ExceptionHandler(DocumentValidationException.class)
    ProblemDetail invalidDocument(DocumentValidationException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail duplicate(DataIntegrityViolationException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Resource already exists or violates a database constraint", request);
    }

    private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("path", request.getRequestURI());
        return problem;
    }
}
