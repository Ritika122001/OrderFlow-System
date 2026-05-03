package com.example.item.ExceptionHandler;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.item.exceptions.ItemNotFoundException;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<String> ItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body(ex.getMessage());
    }

}
