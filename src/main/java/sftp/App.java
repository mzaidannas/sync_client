package sftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;

import sync_clients.*;

public class App {
    private static Logger logger;

    public static void main(String[] args) throws InterruptedException {
        // Creating folder
        String homeDir = System.getProperty("user.home");
        String dirPath = homeDir + "/Desktop/sftp_sync/";
        File dir = new File(dirPath);
        if (! dir.exists()) {
            dir.mkdir();
        }

        // Create logger
        logger = Logger.getLogger(App.class.getName());
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

        copySelf();
        createTask();

        SyncClient syncClient = new Ftp(creds, logger);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    syncClient.disconnect();
                    logger.info("Sending mail on shutdown");
                    new Mailer().send();
                    Thread.sleep(200);
                    logger.info("Shouting down ...");
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

    public static void copySelf() {
        try {
            String dirPath = "C:/Program Files/sync_client";
            File dir = new File(dirPath);
            if (! dir.exists()) {
                dir.mkdir();
            }

            logger.info("Trying to copy jar file to system");
            File source = new java.io.File(App.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath());

            File dest = new File("C:/Program Files/sync_client/SFTP_Sync.exe");

            logger.info(String.format("Copying file %s to %s\n", source.toPath(), dest.toPath()));

            Files.copy(source.toPath(), dest.toPath());
        } catch (IOException ex) {
            logger.severe(ex.getMessage());
            logger.info("Error copying jar file to system");
        }
    }

    public static void createTask() {
        List<String> commands = new ArrayList<String>();

        commands.add("schtasks.exe");
        commands.add("/CREATE");
        commands.add("/TN");
        commands.add("\"SFTP_Sync\"");
        commands.add("/TR");
        commands.add("\"C:/Program Files/sync_client/SFTP_Sync.exe\"");
        commands.add("/SC");
        commands.add("ONLOGON");
        commands.add("/RU");
        commands.add("SYSTEM");
        commands.add("/RL");
        commands.add("HIGHEST");

        try {
            logger.info("Windows thread interrpted");
            ProcessBuilder builder = new ProcessBuilder(commands);
            Process p = builder.start();
            p.waitFor();
            logger.info(String.valueOf(p.exitValue()));
        } catch (InterruptedException ex) {
            logger.info("Windows thread interrpted");
        } catch (IOException ex) {
            logger.info("Error creating login task");
        }
    }
}
