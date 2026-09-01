package com.divvy.autenticacion.infrastructure.email;

import com.divvy.autenticacion.domain.EmailSender;

import java.util.ArrayList;
import java.util.List;

public class FakeEmailSender implements EmailSender {

    private final List<EmailEnviado> enviados = new ArrayList<>();

    @Override
    public synchronized void enviar(String destinatario, String asunto, String cuerpo) {
        enviados.add(new EmailEnviado(destinatario, asunto, cuerpo));
    }

    public synchronized List<EmailEnviado> enviados() {
        return List.copyOf(enviados);
    }

    public synchronized void limpiar() {
        enviados.clear();
    }

    public record EmailEnviado(String destinatario, String asunto, String cuerpo) {
    }
}
