//CCODE INSPIRED BY
//https://www.digitalocean.com/community/tutorials/javamail-example-send-mail-in-java-smtp
//https://stackoverflow.com/questions/67556270/javax-net-ssl-sslhandshakeexception-no-appropriate-protocol-protocol-is-disabl
//Made to work with our App


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.print.attribute.PrintServiceAttribute;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;



public class MailSendingHandler {

	private Mail mail;

    public MailSendingHandler(QNA emailPrompt, QNA sendEmailPrompt, EmailSetupPanel esp){
		this.mail = parseSendEmailPrompt(emailPrompt, sendEmailPrompt, esp);
	}

	private Mail parseSendEmailPrompt(QNA emailPrompt, QNA sendEmailPrompt, EmailSetupPanel esp){
		MailBuilder mb = new MailBuilder();

		String[] splitPromptString = sendEmailPrompt.getQuestion().split(" ");
		String emailBody = emailPrompt.getAnswer();
		String recipientEmail = splitPromptString[3] + "@" + splitPromptString[5];
		
		//sometimes it adds a thing at the end
		if(recipientEmail.charAt(recipientEmail.length() - 1) == '.'){
			recipientEmail = recipientEmail.substring(0,recipientEmail.length()-1);
		}

		mb.setSenderEmail(esp.getEmailFieldContent())
		.setRecipientEmail(recipientEmail)
		.setEmailPassword(esp.getPasswordFieldContent())
		.setSmtpHost(esp.getSmtpHostFieldContent())
		.setSmtpPort(esp.getSmtpPortFieldContent())
		.setSubjectLine("New email from SayIt")
		.setMailbody(emailBody);
		
		return mb.create();
	}

    private Session buildEmailSession(){
        Properties props = new Properties();
		props.put("mail.smtp.host", mail.getSmtpHost()); //SMTP Host !!!!!!!!!!!!!!!!!
		props.put("mail.smtp.port", mail.getSmtpPort()); //TLS Port!!!!!!!!!!!!!!!!!!!
		props.put("mail.smtp.auth", "true"); //enable authentication
		props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");		
                //create Authenticator object to pass in Session.getInstance argument
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mail.getSenderEmail(), mail.getEmailPassword());
			}
		};
		return Session.getInstance(props, auth);
        
    }

    public void sendEmail() throws Exception{
	      MimeMessage msg = new MimeMessage(buildEmailSession());
	      //set message headers
	      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
	      msg.addHeader("format", "flowed");
	      msg.addHeader("Content-Transfer-Encoding", "8bit");

	      msg.setFrom(new InternetAddress(mail.getSenderEmail(), "SayIt Mail"));

	      msg.setReplyTo(InternetAddress.parse(mail.getSenderEmail(), false));

	      msg.setSubject(mail.getMailSubjectLine(), "UTF-8");

	      msg.setText(mail.getMailBody(), "UTF-8");

	      msg.setSentDate(new Date());

	      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getRecipientEmail(), false));
	      System.out.println("Message is ready");
    	  Transport.send(msg);  

	      System.out.println("EMail Sent Successfully!!");
	}

	public Mail getMail(){
		return mail;
	}
}
