package sync_clients;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import sftp.Creds;

public abstract class SyncClient {

    Creds creds;
    Logger logger;

    SyncClient() {
        creds = null;
    }

    SyncClient(Creds creds) {
        this.creds = creds;
        logger = Logger.getLogger(SyncClient.class.getName());
        logger.addHandler(new ConsoleHandler());
        try {
            logger.addHandler(new FileHandler());
        } catch (IOException ex) {
            logger.warning("Error creating a log file");
        }
    }

    SyncClient(Creds creds, Logger logger) {
        this.creds = creds;
        this.logger = logger;
    }

    public abstract void send(String filePath, String remoteDirectory);

    public abstract void delete(String filePath, String remoteDirectory);

    public abstract void connect();

    public abstract void disconnect();
}
