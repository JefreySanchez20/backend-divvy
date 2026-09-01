package com.divvy.autenticacion.domain;

public interface EmailSender {

    void enviar(String destinatario, String asunto, String cuerpo);
}
