package sync_clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import sftp.Creds;

public class Ftp extends SyncClient {

    public FTPClient ftpClient;
    public Ftp(Creds creds, Logger logger) {
        super(creds, logger);

        ftpClient = new FTPClient();
        connect();
    }

    public void send(String filePath, String remoteDirectory) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Changes working directory
            Boolean success = ftpClient.changeWorkingDirectory(remoteDirectory);
            showServerReply(ftpClient);

            if (success) {
                logger.info("Successfully changed working directory.");
            } else {
                logger.info("Failed to change working directory. See server's reply.");
                return;
            }

            File localFile = new File(filePath);
            InputStream inputStream = new FileInputStream(localFile);

            logger.info("Start uploading file");
            OutputStream outputStream = ftpClient.storeFileStream(localFile.getName());
            byte[] bytesIn = new byte[4096];
            int read = 0;

            while ((read = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                logger.info("The file is uploaded successfully.");
            }

        } catch (IOException ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void delete(String filePath, String remoteDirectory) {
        try {
            // Changes working directory
            Boolean success = ftpClient.changeWorkingDirectory(remoteDirectory);
            showServerReply(ftpClient);

            if (success) {
                logger.info("Successfully changed working directory.");
            } else {
                logger.info("Failed to change working directory. See server's reply.");
            }

            File localFile = new File(filePath);
            boolean delete = ftpClient.deleteFile(localFile.getName());

            if (delete) {
                logger.info("Successfully deleted the file.");
            } else {
                logger.info("Unable to delete the file.");
            }
        } catch (IOException ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void connect() {
        try {
            if(!ftpClient.isConnected()) {
                ftpClient.connect(creds.HOST, creds.PORT);
                ftpClient.login(creds.USER, creds.PASSWORD);
                ftpClient.enterLocalPassiveMode();
            }
        } catch(SocketException ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
        } catch(IOException ex) {
            logger.severe("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                logger.info("SERVER: " + aReply);
            }
        }
    }
}
