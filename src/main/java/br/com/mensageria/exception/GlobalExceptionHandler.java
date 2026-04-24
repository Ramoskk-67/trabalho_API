package br.com.mensageria.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;
/**
* @RestControllerAdvice intercepta exceções lançadas pelos controllers
* e monta a resposta HTTP de forma uniforme.
*/

@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(EventoNotFoundException.class)
public ResponseEntity<Map<String, String>> handleEventoNotFound(EventoNotFoundException ex) {
Map<String, String> body = new HashMap<>();
body.put("erro", ex.getMessage());
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
}
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
Map<String, String> erros = new HashMap<>();
ex.getBindingResult().getFieldErrors().forEach(error ->
erros.put(error.getField(), error.getDefaultMessage()));
return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
}
}