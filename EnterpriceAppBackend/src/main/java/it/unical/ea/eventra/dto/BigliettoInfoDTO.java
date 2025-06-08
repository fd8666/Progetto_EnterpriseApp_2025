package it.unical.ea.eventra.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BigliettoInfoDTO {
        private Long id;
        private String nomeSpettatore;
        private String cognomeSpettatore;
        private String emailSpettatore;
        private Long eventoId;
        private String eventoNome;
        private String dataEvento;
        private Long tipoPostoId;
        private String tipoPostoNome;
        private Double prezzo;
        private Long pagamentoId;
        private LocalDateTime dataCreazione;
        private Long ordineId;
        private Long utenteId;
}
