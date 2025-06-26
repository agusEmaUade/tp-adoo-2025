package com.tp.uno.mas.encuentros.deportivos.adapter;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

public class JavaMailAdapter implements ServicioEmail {
    private final Resend resend;
    private final String FROM = "Acme <onboarding@resend.dev>";

    public JavaMailAdapter() {
        this.resend = new Resend("re_eRjJmYuV_M5DJj7hwaL2XeDDo4WHvKARd");
    }

    @Override
    public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(FROM)
                .to("agyanez@uade.edu.ar")
                .subject(asunto)
                .html("<p>" + mensaje + "</p>")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            System.out.println("Email enviado con ID: " + data.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar email con Resend: " + e.getMessage());
            return false;
        }
    }
}