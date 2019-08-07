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
      String to = "m.zaid.annas@gmail.com";

      // Sender's email ID needs to be mentioned
      String from = "roshnaasinc@gmail.com";

      // Assuming you are sending email from localhost
      String host = "localhost";

      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("smtp.gmail.com", host);

      // mail username and password
      properties.setProperty("mail.user", "roshnaasinc@gmail.com");
      properties.setProperty("mail.password", "roshnaas2019");

      // Enable SSL and TLS for mailer
      properties.put("mail.smtp.port", "587");
      properties.put("mail.smtp.auth", "true");
      properties.put("mail.smtp.starttls.enable", "true");
      properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

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
