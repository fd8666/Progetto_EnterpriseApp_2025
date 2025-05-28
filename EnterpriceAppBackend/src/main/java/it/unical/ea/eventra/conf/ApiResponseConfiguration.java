package it.unical.ea.eventra.conf;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponseConfiguration<T> {
    private boolean success;
    private String message;
    private T data;
}
