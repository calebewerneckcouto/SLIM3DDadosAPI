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

    // Sobrescrevendo corretamente o método handleExceptionInternal
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        
        // Usando StringBuilder para construção eficiente da mensagem de erro
        StringBuilder msg = new StringBuilder();
        
        // Verificando se é uma exceção de validação
        if (ex instanceof MethodArgumentNotValidException) {
            List<ObjectError> list = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
            for (ObjectError objectError : list) {
                msg.append(objectError.getDefaultMessage()).append("\n");
            }
        } else {
            msg.append(ex.getMessage());
        }
        
        // Criando um objeto de erro com a mensagem e o código de status
        ObjetoErro objetoErro = new ObjetoErro();
        objetoErro.setError(msg.toString());
        objetoErro.setCode(status.value() + " ==> " + status.getReasonPhrase());
        
        // Retorna o ResponseEntity com os detalhes do erro
        return new ResponseEntity<>(objetoErro, headers, status);
    }

    // Também podemos sobrescrever outras exceções específicas
    @ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
