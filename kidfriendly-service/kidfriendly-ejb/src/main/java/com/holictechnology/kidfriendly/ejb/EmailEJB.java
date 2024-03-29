package com.holictechnology.kidfriendly.ejb;


import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.holictechnology.kidfriendly.domain.dto.EmailDto;
import com.holictechnology.kidfriendly.domain.dto.EmailDto.Recipient;
import com.holictechnology.kidfriendly.ejb.interfaces.EmailLocal;
import com.holictechnology.kidfriendly.library.messages.KidFriendlyMessages;
import com.holictechnology.kidfriendly.library.utilites.ObjectUtilities;


@Stateless
public class EmailEJB extends AbstractEJB implements EmailLocal {

    private static final long serialVersionUID = -4488307229929149557L;
    private static final String HOST_NAME = "mail.kidfriendly.com.br";
    private static final int SMTP_PORT = 25;
    private static final String USERNAME = "contatos@kidfriendly.com.br";
    private static final String PASSWORD = "@123456holic";

    @Override
    @Asynchronous
    @Transactional(value = TxType.NOT_SUPPORTED)
    public void sendSimpleEmail(EmailDto emailDto) {
        try {
            illegalArgument(emailDto);
            SimpleEmail simpleEmail = new SimpleEmail();
            simpleEmail.setMsg(ObjectUtilities.utf8(emailDto.getMessage()));
            send(simpleEmail, emailDto);
        } catch (Exception exception) {
            getLogger(getClass()).error(exception.getMessage(), exception);
        }
    }

    /**
     * @param email
     * @param emailDto
     * @throws EmailException
     */
    private void send(Email email, EmailDto emailDto) throws EmailException {
        if (email == null) {
            return;
        }

        email.setHostName(HOST_NAME);
        email.setSmtpPort(SMTP_PORT);
        email.setStartTLSEnabled(Boolean.TRUE);
        email.setSSLOnConnect(Boolean.TRUE);
        email.setAuthenticator(new DefaultAuthenticator(USERNAME, PASSWORD));
        email.setFrom(emailDto.getFromEmail(), ObjectUtilities.utf8(emailDto.getFromName()));

        if (emailDto.getRecipients() != null && !emailDto.getRecipients().isEmpty()) {
            for (Recipient recipient : emailDto.getRecipients()) {
                email.addTo(recipient.getEmail(), ObjectUtilities.utf8(recipient.getName()));
            }
        } else {
            throw new EmailException(KidFriendlyMessages.ERROR_EMAIL_NOT_RECIPIENT);
        }

        email.setSubject(ObjectUtilities.utf8(emailDto.getSubject()));
        email.send();
    }
}
