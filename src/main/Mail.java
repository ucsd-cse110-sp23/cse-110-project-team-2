public class Mail {
    
    private String smtpHost;
    private String smtpPort;
    private String senderEmail;
    private String emailPassword;
    private String recipientEmail;
    private String mailSubjectLine;
    private String mailBody; 

    public Mail(){
    }

    public void setSmtpHost(String smtpHost){
        this.smtpHost = smtpHost;
    }
    
    public void setSmtpPort(String smtpPort){
        this.smtpPort = smtpPort;
    }

    public void setSenderEmail(String senderEmail){
        this.senderEmail = senderEmail;
    }

    public void setRecipientEmail(String recipientEmail){
        this.recipientEmail = recipientEmail;
    }

    public void setEmailPassword(String emailPassword){
        this.emailPassword = emailPassword;
    }

    public void setSubjectLine(String mailSubjectLine){
        this.mailSubjectLine = mailSubjectLine;
    }

    public void setMailbody(String mailBody){
        this.mailBody = mailBody;
    }

    public String getSmtpHost(){
        return smtpHost;
    }
    
    public String getSmtpPort(){
        return smtpPort;
    }

    public String getSenderEmail(){
        return senderEmail;
    }

    public String getEmailPassword(){
        return emailPassword;
    }

    public String getMailSubjectLine(){
        return mailSubjectLine;
    }

    public String getMailBody(){
        return mailBody;
    }

    public String getRecipientEmail(){
        return recipientEmail;
    }

    public boolean equals(Mail otherMail){
        return otherMail.toString().equals(this.toString());
    }

    public String toString(){
        return "host: " + this.getSmtpHost() + "\n port: " + this.getSmtpPort() + "\n sender email: " + this.getSenderEmail() + "\n email pw: " + this.getEmailPassword() + "\n recipientEmail: " + this.getRecipientEmail() + "\n mail body: " + this.getMailBody() + "\n subject line: " + this.getMailSubjectLine();
    }
}
