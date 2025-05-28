package it.unical.ea.eventra.core;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import it.unical.ea.eventra.data.entity.Biglietto;
import it.unical.ea.eventra.data.entity.Pagamento;
import it.unical.ea.eventra.data.entity.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;



//Ottiene le informazioni e le sostituisce alle risorse dinamiche ($)
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final VelocityEngine velocityEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, VelocityEngine velocityEngine) {
        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
    }

    public void sendBigliettoConferma(Biglietto biglietto) {
        try {
            MimeMessageHelper helper = prepareEmail(biglietto.getEmailSpettatore(), "Conferma Acquisto Biglietto");

            VelocityContext context = prepareBigliettoContext(biglietto);
            addCalendarInfo(context, biglietto);

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            context.put("baseUrl", baseUrl);

            context.put("eventoId", biglietto.getEvento().getId());

            StringWriter writer = new StringWriter();
            velocityEngine.getTemplate("Templates/Email/ticketConfirmationEmail.vm").merge(context, writer);

            helper.setText(writer.toString(), true);
            addCommonInlineImages(helper);

            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma", e);
        }
    }

    public void sendModificaBigliettoConferma(Biglietto biglietto) {
        try {
            MimeMessageHelper helper = prepareEmail(biglietto.getEmailSpettatore(), "Conferma Modifica Biglietto");

            VelocityContext context = prepareBigliettoContext(biglietto);
            context.put("email", biglietto.getEmailSpettatore());
            addCalendarInfo(context, biglietto);

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            context.put("baseUrl", baseUrl);

            context.put("eventoId", biglietto.getEvento().getId());

            StringWriter writer = new StringWriter();
            velocityEngine.getTemplate("Templates/Email/ticketModificationAlert.vm").merge(context, writer);

            helper.setText(writer.toString(), true);
            addCommonInlineImages(helper);

            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma", e);
        }
    }

    public void sendRegistrazioneConferma(Utente utente) {
        try {
            MimeMessageHelper helper = prepareEmail(utente.getEmail(), "Registrazione Avvenuta con Successo");

            VelocityContext context = new VelocityContext();
            context.put("nome", utente.getNome());
            context.put("cognome", utente.getCognome());
            context.put("email", utente.getEmail());

            StringWriter writer = new StringWriter();
            velocityEngine.getTemplate("Templates/Email/registrationAlert.vm").merge(context, writer);

            helper.setText(writer.toString(), true);
            helper.addInline("logo", new ClassPathResource("static/images/logo.png"));

            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma", e);
        }
    }

    public void sendOrdineConferma(Pagamento pagamento) {
        try {
            MimeMessageHelper helper = prepareEmail(pagamento.getOrdine().getEmailProprietario(), "Ordine Avvenuto con Successo");

            VelocityContext context = new VelocityContext();
            context.put("nomePropietario", pagamento.getOrdine().getProprietario().getNome());
            context.put("totale", pagamento.getOrdine().getPrezzoTotale());
            context.put("dataConferma", pagamento.getOrdine().getDataCreazione());

            StringWriter writer = new StringWriter();
            velocityEngine.getTemplate("Templates/Email/orderCofermationEmail.vm").merge(context, writer);

            helper.setText(writer.toString(), true);
            helper.addInline("logo", new ClassPathResource("static/images/logo.png"));

            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("Errore nell'invio dell'email di conferma", e);
        }
    }

    private VelocityContext prepareBigliettoContext(Biglietto biglietto) {
        VelocityContext context = new VelocityContext();
        context.put("nome", biglietto.getNomeSpettatore());
        context.put("cognome", biglietto.getCognomeSpettatore());
        context.put("evento", biglietto.getEvento().getNome());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = biglietto.getEvento().getDataOraEvento().format(formatter);
        context.put("data", formattedDate);

        context.put("luogo", biglietto.getEvento().getStruttura().getNome());
        context.put("indirizzo", biglietto.getEvento().getStruttura().getIndirizzo());
        context.put("posto", biglietto.getTipoPosto().getNome());
        context.put("zona", biglietto.getTipoPosto().getFeatures().getZona());
        context.put("features", biglietto.getTipoPosto().getFeatures().getFeatures());
        return context;
    }

    private void addCalendarInfo(VelocityContext context, Biglietto biglietto) {
        DateTimeFormatter calendarFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        ZonedDateTime start = biglietto.getEvento().getDataOraEvento()
                .atZone(ZoneId.of("Europe/Rome")).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime end = start.plusHours(2);

        context.put("formattedStart", start.format(calendarFormatter));
        context.put("formattedEnd", end.format(calendarFormatter));
    }

    private MimeMessageHelper prepareEmail(String to, String subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        return helper;
    }

    private void addCommonInlineImages(MimeMessageHelper helper) throws MessagingException {
        helper.addInline("logo", new ClassPathResource("static/images/logo.png"));
        helper.addInline("pin", new ClassPathResource("static/images/pin.png"));
        helper.addInline("calendar", new ClassPathResource("static/images/calendar.png"));
    }
}
