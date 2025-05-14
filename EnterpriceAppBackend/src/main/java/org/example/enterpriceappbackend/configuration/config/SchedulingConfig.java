package org.example.enterpriceappbackend.configuration.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enterpriceappbackend.data.service.BigliettoService;
import org.example.enterpriceappbackend.data.service.EventoService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SchedulingConfig {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventoService eventoService;
    private final BigliettoService bigliettoService;

    /**
     * Scheduler che esegue il controllo degli eventi scaduti ogni giorno a mezzanotte
     */
    @Scheduled(cron = "0 0 3 * * ?") // Esegui ogni giorno a mezzanotte
    public void checkEventiScadutiSchedule() {
        log.info(String.format("Controllo eventi scaduti avviato alle [%s]", formatter.format(LocalDateTime.now())));
        eventoService.checkEventiScaduti();
    }

    /**
     * Scheduler che esegue il controllo dei biglietti non pagati ogni giorno a mezzanotte
     */
    @Scheduled(cron = "0 * * * * ?") // Esegui ogni giorno a mezzanotte
    public void checkBigliettiScadutiSchedule() {
        log.info(String.format("Controllo biglietti non pagati avviato alle [%s]", formatter.format(LocalDateTime.now())));
        bigliettoService.checkBigliettiScaduti();
    }

}
