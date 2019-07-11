package sftp;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;

import sync_clients.*;

public class App {

    public static void main(String[] args) {
        // Creating folder
        String homeDir = System.getProperty("user.home");
        String dirPath = homeDir + "/Desktop/sftp_sync/";
        File dir = new File(dirPath);
        if (! dir.exists()) {
            dir.mkdir();
        }

        // Create logger
        Logger logger = Logger.getLogger(App.class.getName());
        logger.addHandler(new ConsoleHandler());
        try {
            logger.addHandler(new FileHandler(dirPath + "log.txt"));
        } catch (IOException ex) {
            logger.warning("Error creating a log file");
        }

        // Creds
        String credsPath = dirPath + "creds.yml";
        File tmpDir = new File(credsPath);
        boolean exists = tmpDir.exists();

        final Creds creds;
        if (exists) {
            creds = Creds.readFromFile(credsPath);
        }
        else {
            logger.info("Installation begin");
            creds = new Creds();
            creds.readFromUser(); // TODO: ensure user correct input
            Creds.writeToFile(creds, credsPath);
            logger.info("Installation finished");
        }

        SyncClient syncClient = new Sftp(creds, logger);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    logger.info("Shouting down ...");
                    syncClient.disconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Watcher watcher = new Watcher(syncClient, logger);

        try {
            watcher.watch(dirPath, creds.DIRECTORY);
        } catch(Exception ex) {
            logger.severe("Program exception catched");
            logger.severe(ex.getMessage());
            ex.printStackTrace();
        } finally {
            logger.info("Program stopped");
            syncClient.disconnect();
        }
    }

    public String getGreeting() {
        return "Hello World";
    }
}
