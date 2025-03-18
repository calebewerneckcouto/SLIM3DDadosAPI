package com.devsuperior.cwcdev;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControleExcecoes extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        
        // Using StringBuilder for efficient message construction
        StringBuilder msg = new StringBuilder();
        
        if (ex instanceof MethodArgumentNotValidException) {
            List<ObjectError> list = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
            for (ObjectError objectError : list) {
                msg.append(objectError.getDefaultMessage()).append("\n");
            }
        } else {
            msg.append(ex.getMessage());
        }
        
        // Create an error object with the message and status code
        ObjetoErro objetoErro = new ObjetoErro();
        objetoErro.setError(msg.toString());
        objetoErro.setCode(status.value() + " ==> " + status.getReasonPhrase());
        
        // Return the ResponseEntity with error details
        return new ResponseEntity<>(objetoErro, headers, status);
    }

}
