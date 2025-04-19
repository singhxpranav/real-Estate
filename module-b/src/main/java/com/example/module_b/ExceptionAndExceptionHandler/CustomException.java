package com.example.module_b.ExceptionAndExceptionHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CustomException extends RuntimeException {
    private String errorMessage;
    public CustomException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }
}
