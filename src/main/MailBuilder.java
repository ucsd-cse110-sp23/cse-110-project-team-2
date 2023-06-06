public class MailBuilder {
    
    private Mail mail; 

    public MailBuilder(){
        mail = new Mail();
    }

    public Mail create(){
        return mail;
    }

    public MailBuilder setSmtpHost(String smtpHost){
        mail.setSmtpHost(smtpHost);
        return this;
    }
    
    public MailBuilder setSmtpPort(String smtpPort){
        mail.setSmtpPort(smtpPort);
        return this;
    }

    public MailBuilder setSenderEmail(String senderEmail){
        mail.setSenderEmail(senderEmail);
        return this;
    }

    public MailBuilder setRecipientEmail(String recipientEmail){
        mail.setRecipientEmail(recipientEmail);
        return this;
    }

    public MailBuilder setEmailPassword(String emailPassword){
        mail.setEmailPassword(emailPassword);
        return this;
    }

    public MailBuilder setSubjectLine(String mailSubjectLine){
        mail.setSubjectLine(mailSubjectLine);
        return this;
    }

    public MailBuilder setMailbody(String mailBody){
        mail.setMailbody(mailBody);
        return this;
    }


    
}
