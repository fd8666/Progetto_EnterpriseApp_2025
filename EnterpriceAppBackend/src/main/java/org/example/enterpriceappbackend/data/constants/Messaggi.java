package org.example.enterpriceappbackend.data.constants;

import org.springframework.stereotype.Component;

@Component
public class Messaggi {

    public String recuperoPassword(String nome, String password){
        if(password.isEmpty()){
            return " account autenticato esternamente con google /o facebook, usa la recovery password del tuo metodo di autenticazione scelto, cordiali saluti !";
        }
        return "ciao " + nome + ",\n\n"
                + "la tua nuova password temporanea è: " + password + "\n"
                + "ti consigliamo di cambiarla dopo il primo accesso.\n\n"
                + "cordiali saluti, \n";
    }

    public String Benvenuto(String nome){
        return "Benvenuto " + nome + "! \n\n Siamo lieti che tu ti sia registrato alla nostra applicazione! ";
    }

    public String ConfermaRegistrazione(String nUtente){
        return "Caro/a" + nUtente + ", \n\n"
                + "Grazie per esserti registrato/a alla nostra applicazione, siamo felici di averti con noi!! \n"
                + "Cordiali saluti !";
    }
}
