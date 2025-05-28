package it.unical.ea.eventra.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import it.unical.ea.eventra.data.entity.Biglietto;
import it.unical.ea.eventra.data.entity.Struttura;
import it.unical.ea.eventra.data.entity.TipoPosto;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {

    private Long id;
    private String nome;
    private String descrizione;
    private Long categoriaId;
    private String immagine;
    private LocalDateTime dataOraEvento;
    private LocalDateTime dataOraAperturaCancelli;
    private Integer postiDisponibili;
    private String luogo;
    private Long organizzatoreId;
    private List<Biglietto> biglietti;
    private Struttura struttura;
    private List<TipoPosto> tipiPosto;

}
