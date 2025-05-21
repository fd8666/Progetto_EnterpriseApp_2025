package org.example.enterpriceappbackend.configuration.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.example.enterpriceappbackend.exceptions.BadRequest;
import org.example.enterpriceappbackend.exceptions.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

    //mi serve per la gestione di un limite minimo di caratteri nella ricerca delle strutture da parte dell'organizzatore ecc
    @ExceptionHandler(BadRequest.class)
   // Restituisce un errore 400 con messaggio personalizzato in base all'api con eccezione gestito nel service
    public ResponseEntity<String> handleBadRequest(BadRequest badRequest) {
        return new ResponseEntity<>(badRequest.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //per avere 404 con messaggi personalizzati e non quello base
    @ExceptionHandler(NotFound.class)
    public ResponseEntity<String> handleNotFoundException(NotFound notFound) {
        return new ResponseEntity<>(notFound.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Gestisce errori di validazione di @Valid su DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationError(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Dati non validi";
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    // Gestisce vincoli violati su singoli parametri (es. @Positive)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Violazione di vincolo");
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedExceptions(Exception ex) {
        return new ResponseEntity<>("Si è verificato un errore interno. Riprova più tardi.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
