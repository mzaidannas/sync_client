package sftp;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {

   public void send() {
      // Recipient's email ID needs to be mentioned.
      String to = "touser@gmail.com";

      // Sender's email ID needs to be mentioned
      String from = "fromuser@gmail.com";

      // Assuming you are sending email from localhost
      String host = "smtp.gmail.com";

      // SMTP server port
      String port = "587";

      // SSL port
      // String ssl_port = "443";

      // Get system properties
      Properties properties = System.getProperties();

      // mail username and password
      properties.setProperty("mail.user", "fromuser@gmail.com");
      properties.setProperty("mail.password", "fromuser_password");

      // Enable host and port for smtp mailer
      properties.setProperty("mail.smtp.host", host);
      properties.setProperty("mail.smtp.port", port);

      // If using authentication
      properties.setProperty("mail.smtp.auth", "true");
      properties.setProperty("mail.smtp.starttls.enable", "true");

      // If using SSL
      // properties.setProperty("mail.smtp.socketFactory.port", ssl_port);
      // properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      // properties.setProperty("mail.smtp.socketFactory.fallback", "false");

      // Get the default Session object.
      Session session = Session.getInstance(properties,
          new javax.mail.Authenticator() {

            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.user"),
                                                  properties.getProperty("mail.password"));
            }
          });

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("This is the Subject Line!");

         // Send the actual HTML message, as big as you like
         message.setContent("<h1>This is actual message</h1>", "text/html");

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}
