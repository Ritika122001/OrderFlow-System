package com.example.order.ExceptionHandler;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.order.exceptions.ItemNotFoundException;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> ItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(ex.getMessage());
    }

}
