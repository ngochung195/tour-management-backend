package com.example.tour_management.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    //  VALIDATION ERROR
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        log.error("Lỗi validate dữ liệu", ex);

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "message", "Dữ liệu nhập không hợp lệ",
                        "errors", errors
                ));
    }


    //  LOGIN SAI
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {

        log.error("Sai thông tin đăng nhập", ex);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 401,
                        "message", "Sai email hoặc mật khẩu"
                ));
    }

    //  CHƯA ĐĂNG NHẬP (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleUnauthorized(AuthenticationException ex) {

        log.error("Chưa đăng nhập", ex);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 401,
                        "message", "Vui lòng đăng nhập để truy cập tài nguyên này"
                ));
    }

    //  KHÔNG CÓ QUYỀN (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleForbidden(AccessDeniedException ex) {

        log.error("Không có quyền truy cập", ex);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 403,
                        "message", "Bạn không có quyền truy cập tài nguyên này"
                ));
    }

    //  BAD REQUEST
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {

        log.error("Lỗi request", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 400,
                        "message", ex.getMessage()
                ));
    }

    //  NOT FOUND
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {

        log.error("Không tìm thấy dữ liệu", ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "message", ex.getMessage()
                ));
    }

    //  RESOURCE NOT FOUND
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResource(NoResourceFoundException ex) {

        log.warn("Không tìm thấy resource: {}", ex.getResourcePath());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 404,
                        "message", "Không tìm thấy tài nguyên"
                ));
    }

    //  LỖI HỆ THỐNG
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex) {

        log.error("Lỗi hệ thống", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", 500,
                        "message", "Lỗi hệ thống, vui lòng thử lại sau"
                ));
    }
}