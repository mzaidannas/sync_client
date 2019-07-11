package sync_clients;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import com.jcraft.jsch.*;

import sftp.Creds;

public class Sftp extends SyncClient {

    Session session = null;
    Channel channel = null;
    ChannelSftp channelSftp = null;

    public Sftp(Creds creds, Logger logger) {
        super(creds, logger);
        connect();
    }

    public void send(String filePath, String remoteDirectory) {
        try {
            channelSftp.cd(remoteDirectory);
        } catch (SftpException ex) {
            logger.warning(String.format("Could not change directory to %s%n", remoteDirectory));
            return;
        }
        try {
            File f = new File(filePath);
            channelSftp.put(new FileInputStream(f), f.getName());
            logger.info("File transferred successfully to host.");
        } catch (NullPointerException ex) {
            logger.warning(String.format("File not found on path %s%n", filePath));
        } catch (SftpException ex) {
            logger.warning(String.format("Could write file %s to directory %s%n", filePath, remoteDirectory));
        } catch (Exception ex) {
            logger.severe("Exception found while writing file to server.");
        }
    }

    public void delete(String filePath, String remoteDirectory) {
        try {
            channelSftp.cd(remoteDirectory);
        } catch (SftpException ex) {
            logger.warning(String.format("Could not change directory to %s%n", remoteDirectory));
            return;
        }
        try {
            File localFile = new File(filePath);
            channelSftp.rm(localFile.getName());
            logger.info("File deleted successfully from host.");
        } catch (NullPointerException ex) {
            logger.warning(String.format("File not found on path %s%n", filePath));
        } catch (SftpException ex) {
            logger.warning(String.format("Could delete file %s from directory %s%n", filePath, remoteDirectory));
        } catch (Exception ex) {
            logger.severe("Exception found while deleting the file.");
        }
    }

    public void connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(creds.USER, creds.HOST, creds.PORT);
            logger.info("SFTP session created successfully");
        }
        catch (JSchException ex) {
            logger.warning("Invalid Credentials");
            return;
        }
        try {
            session.setPassword(creds.PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            logger.info("Host connected. Password accepted");
        } catch (JSchException ex) {
            logger.warning("Invalid Password");
            return;
        }
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            logger.info("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            logger.info("preparing the host information for sftp.");
        } catch (JSchException ex) {
            logger.warning("Unable to open SFTP channel");
        }
    }

    public void disconnect() {
        channelSftp.exit();
        logger.info("sftp Channel exited.");
        channel.disconnect();
        logger.info("Channel disconnected.");
        session.disconnect();
        logger.info("Host Session disconnected.");
    }
}
