import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.Properties;

public class EmailExtractor {

    public static void main(String[] args) {
        // Email account details
        String host = "outlook.office365.com";
        String username = "email@outlook.com";
        String password = "pws";

        // Directory to save emails and attachments
        String saveDirectory = System.getProperty("user.home") + "/OneDrive/Desktop/Java/Data";

        // Mail server properties
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.imap.ssl.enable", "true"); // Ensure SSL/TLS is enabled

        try {
            // Connect to the email server
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            session.setDebug(true); // Enable debugging

            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            // Access the inbox folder
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Retrieve messages from the inbox
            Message[] messages = inbox.getMessages();

            // Process each message
            for (Message message : messages) {
                String subject = message.getSubject();
                System.out.println("Processing message: " + subject);

                // Create a folder for each message
                String messageFolder = saveDirectory + File.separator + subject.replaceAll("[^a-zA-Z0-9.-]", "_");
                File folder = new File(messageFolder);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                // Extract content from the message
                //Multipart and BodyPart are both classes provided by the JavaMail API, which is a Java library used for sending and receiving email via SMTP, POP3, and IMAP protocols.
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    String disposition = bodyPart.getDisposition();

                    // Save attachments or email text
                    if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                        // Save attachment within the message folder
                        InputStream inputStream = bodyPart.getInputStream();
                        String fileName = bodyPart.getFileName();
                        saveFile(inputStream, messageFolder + File.separator + fileName);
                    } else {
                        // Save email text within the message folder
                        String emailText = getText(bodyPart);
                        saveEmailText(emailText, messageFolder, subject);
                    }
                }
            }

            inbox.close(true);
            store.close();

            System.out.println("Emails and attachments saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to save attachment files
    private static void saveFile(InputStream inputStream, String filePath) throws IOException {
        OutputStream outputStream = new FileOutputStream(new File(filePath));
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
    }

    // Method to extract text content from email
    private static String getText(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/*")) {
            return (String) part.getContent();
        } else if (part.isMimeType("multipart/alternative")) {
            Multipart mp = (Multipart) part.getContent();
            String text = null;

            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null) {
                        text = getText(bp);
                    }
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null) {
                        return s;
                    }
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String text = getText(mp.getBodyPart(i));
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    // Method to save email text content
    private static void saveEmailText(String emailText, String directory, String subject) throws IOException {
        if (emailText != null) {
            String fileName = subject.replaceAll("[^a-zA-Z0-9.-]", "_") + ".html"; // Change file extension if necessary
            FileWriter fileWriter = new FileWriter(new File(directory, fileName));
            fileWriter.write(emailText);
            fileWriter.close();
            System.out.println("Email text saved: " + fileName);
        } else {
            System.out.println("Email text is null, skipping saving...");
        }
    }
}
