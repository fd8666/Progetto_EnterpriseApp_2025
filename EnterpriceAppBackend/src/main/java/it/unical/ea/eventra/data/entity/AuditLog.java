package it.unical.ea.eventra.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String method;
    private String endpoint;
    private int responseStatus;
    private String ipAddress;
    private LocalDateTime timestamp;

    @Lob
    private String requestBody;

    private String queryParams;

}
