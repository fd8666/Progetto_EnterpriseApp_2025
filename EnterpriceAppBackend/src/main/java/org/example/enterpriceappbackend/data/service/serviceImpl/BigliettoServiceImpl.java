package org.example.enterpriceappbackend.data.service.serviceImpl;


import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.entity.Biglietto;
import org.example.enterpriceappbackend.data.repository.BigliettoRepository;
import org.example.enterpriceappbackend.data.service.BigliettoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BigliettoServiceImpl implements BigliettoService {

    private final BigliettoRepository bigliettoRepository;

    public void checkBigliettiScaduti() {
        // Trova tutti gli eventi passati
        List<Biglietto> biglietti = bigliettoRepository.findAll();

        biglietti.forEach(biglietto -> {
            if (biglietto.getEvento().getDataOraEvento().isBefore(LocalDateTime.now())) {
                // Se l'evento è passato, il biglietto è scaduto
                System.out.println("Biglietto scaduto per l'evento: " + biglietto.getEvento().getNome());
                biglietto.setDeleted(1);
                bigliettoRepository.save(biglietto);

            }
        });
    }
}
